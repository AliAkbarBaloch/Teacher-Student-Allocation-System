package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.*;
import static de.unipassau.allocationsystem.allocation.AllocationHelper.*;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for performing teacher allocation algorithm.
 * Implements a sophisticated allocation algorithm for a university's teacher internship supervision system,
 * based on the Bavarian educational model.
 * 
 * The algorithm automatically assigns teachers to supervise student internships by creating TeacherAssignment
 * records within an AllocationPlan. The allocation adheres to a strict hierarchy of priorities and a complex
 * set of business rules.
 * 
 * Priority Order:
 * 1. SFP (Study-accompanying subject-didactic internship) - Highest priority
 * 2. ZSP (Additional study-accompanying internship) - Medium priority
 * 3. PDP (Pedagogical-didactic block internship) - Lowest priority
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherAllocationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AllocationDataLoader dataLoader;

    private final TeacherAssignmentRepository teacherAssignmentRepository;

    /**
     * Main orchestration method that orchestrates the entire allocation process.
     * Defaults isCurrent to false for backward compatibility.
     * 
     * @param academicYearId The ID of the academic year for allocation
     * @return AllocationPlan containing all teacher assignments
     * @throws IllegalArgumentException if the academic year is invalid or locked
     */
    @Transactional
    public AllocationPlan performAllocation(Long academicYearId) {
        return performAllocation(academicYearId, false, null);
    }

    /**
     * Main orchestration method that orchestrates the entire allocation process.
     * 
     * @param academicYearId The ID of the academic year for allocation
     * @param isCurrent Whether this plan should be marked as current
     * @return AllocationPlan containing all teacher assignments
     * @throws IllegalArgumentException if the academic year is invalid or locked
     */
    @Transactional
    public AllocationPlan performAllocation(Long academicYearId, Boolean isCurrent) {
        return performAllocation(academicYearId, isCurrent, null);
    }

    /**
     * Main orchestration method that orchestrates the entire allocation process.
     * 
     * @param academicYearId The ID of the academic year for allocation
     * @param isCurrent Whether this plan should be marked as current
     * @param customVersion Custom version string (optional). If null, auto-generates version.
     * @return AllocationPlan containing all teacher assignments
     * @throws IllegalArgumentException if the academic year is invalid or locked
     */
    @Transactional
    public AllocationPlan performAllocation(Long academicYearId, Boolean isCurrent, String customVersion) {
        log.info("=== Starting Allocation Process for Academic Year ID: {}, isCurrent: {}, customVersion: {} ===", 
                academicYearId, isCurrent, customVersion);
        
        // Step 1: Validate that the AcademicYear exists and is not locked
        AcademicYear academicYear = entityManager.find(AcademicYear.class, academicYearId);
        if (academicYear == null) {
            throw new IllegalArgumentException("Academic year with ID " + academicYearId + " not found");
        }
        if (Boolean.TRUE.equals(academicYear.getIsLocked())) {
            throw new IllegalArgumentException("Academic year is locked and cannot be modified");
        }

        // Step 2: Use custom version or generate unique version number
        String newVersion;
        if (customVersion != null && !customVersion.trim().isEmpty()) {
            newVersion = customVersion.trim();
            log.info("Using custom plan version: {}", newVersion);
        } else {
            newVersion = generateNextVersion(academicYearId);
            log.info("Generated plan version: {}", newVersion);
        }

        // Step 3: Create a new AllocationPlan entity with status 'DRAFT'
        AllocationPlan allocationPlan = new AllocationPlan();
        allocationPlan.setAcademicYear(academicYear);
        allocationPlan.setPlanName("Allocation Plan for " + academicYear.getYearName() + " v" + newVersion);
        allocationPlan.setPlanVersion(newVersion);
        allocationPlan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        allocationPlan.setIsCurrent(isCurrent != null ? isCurrent : false);
        allocationPlan.setCreatedAt(LocalDateTime.now());
        allocationPlan.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(allocationPlan);
        entityManager.flush(); // Flush to ensure the plan gets an ID before creating assignments
        log.info("Created allocation plan with ID: {}", allocationPlan.getId());

        // Step 3.5: Clear any existing assignments for this allocation plan (defensive, should be empty for new plan)
        teacherAssignmentRepository.deleteAll(
            teacherAssignmentRepository.findByAllocationPlanId(allocationPlan.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent()
        );

        // Step 4: Load all necessary data from the database into memory for efficient processing
        List<Teacher> teachers = dataLoader.loadAvailableTeachers(academicYearId);
        List<InternshipDemand> demands = dataLoader.loadInternshipDemands(academicYearId);
        Map<Long, List<TeacherQualification>> teacherQualifications = dataLoader.loadTeacherQualifications();
        Map<Long, List<TeacherSubjectExclusion>> teacherExclusions = dataLoader.loadTeacherExclusions(academicYearId);
        Map<Long, List<TeacherAvailability>> teacherAvailabilities = dataLoader.loadTeacherAvailabilities(academicYearId);
        Map<Long, List<TeacherSubject>> teacherSubjects = dataLoader.loadTeacherSubjects(academicYearId);
        List<InternshipType> internshipTypes = dataLoader.loadInternshipTypes();

        Map<Integer, List<ZoneConstraint>> zoneConstraints = dataLoader.loadZoneConstraints();
        Map<Long, List<InternshipCombinationRule>> combinationRules = dataLoader.loadCombinationRules();

        log.info("Loaded data - Teachers: {}, Demands: {}, InternshipTypes: {}", 
            teachers.size(), demands.size(), internshipTypes.size());
        log.info("Teacher Qualifications: {}, Availabilities: {}, Subjects: {}", 
            teacherQualifications.size(), teacherAvailabilities.size(), teacherSubjects.size());
        
        // ADD THIS: Log if there are no demands
        if (demands.isEmpty()) {
            log.warn("WARNING: No internship demands found for academic year ID: {}. No assignments will be created.", academicYearId);
            log.warn("Please check if InternshipDemand records exist for academic year ID: {}", academicYearId);
        } else {
            log.info("Found {} internship demands. Processing allocation...", demands.size());
            // Log demand breakdown by type
            Map<String, Long> demandsByType = demands.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getInternshipType().getInternshipCode(),
                    Collectors.counting()
                ));
            log.info("Demands breakdown: {}", demandsByType);
        }
        
        // ADD THIS: Log if there are no teachers
        if (teachers.isEmpty()) {
            log.warn("WARNING: No available teachers found for academic year ID: {}. No assignments will be created.", academicYearId);
        }

        // Step 5: Initialize tracking structures to monitor the number of assignments per teacher
        Map<Teacher, Integer> assignmentsCount = new HashMap<>();
        Map<Teacher, List<InternshipType>> assignedTypes = new HashMap<>();
        for (Teacher teacher : teachers) {
            assignmentsCount.put(teacher, 0);
            assignedTypes.put(teacher, new ArrayList<>());
        }

        // Step 6: Execute the allocation methods in strict priority order
        // First: SFP allocation (Priority 1)
        InternshipType sfpType = getInternshipTypeByCode("SFP", internshipTypes);
        if (sfpType != null) {
            List<InternshipDemand> sfpDemands = demands.stream()
                    .filter(d -> d.getInternshipType().getId().equals(sfpType.getId()))
                    .collect(Collectors.toList());
            log.info("=== Starting SFP Allocation === (Demands: {})", sfpDemands.size());
            if (sfpDemands.isEmpty()) {
                log.warn("No SFP demands found. Skipping SFP allocation.");
            } else {
                allocateTeachersForSFP(
                        allocationPlan,
                        teachers,
                        sfpDemands,
                        teacherQualifications,
                        teacherExclusions,
                        teacherAvailabilities,
                        teacherSubjects,
                        zoneConstraints,
                        assignmentsCount,
                        assignedTypes,
                        combinationRules
                );
            }
        } else {
            log.warn("SFP InternshipType not found!");
        }

        // Second: ZSP allocation (Priority 2)
        InternshipType zspType = getInternshipTypeByCode("ZSP", internshipTypes);
        if (zspType != null) {
            List<InternshipDemand> zspDemands = demands.stream()
                    .filter(d -> d.getInternshipType().getId().equals(zspType.getId()))
                    .collect(Collectors.toList());
            allocateTeachersForZSP(
                    allocationPlan,
                    teachers,
                    zspDemands,
                    teacherQualifications,
                    teacherExclusions,
                    teacherAvailabilities,
                    teacherSubjects,
                    zoneConstraints,
                    assignmentsCount,
                    assignedTypes,
                    combinationRules
            );
        }

        // Third: PDP allocation (Priority 3)
        InternshipType pdp1Type = getInternshipTypeByCode("PDP1", internshipTypes);
        if (pdp1Type != null) {
            List<InternshipDemand> pdp1Demands = demands.stream()
                    .filter(d -> d.getInternshipType().getId().equals(pdp1Type.getId()))
                    .collect(Collectors.toList());
            allocateTeachersForPDP(
                    allocationPlan,
                    teachers,
                    pdp1Demands,
                    teacherQualifications,
                    teacherExclusions,
                    teacherAvailabilities,
                    teacherSubjects,
                    zoneConstraints,
                    assignmentsCount,
                    assignedTypes,
                    combinationRules
            );
        }

        InternshipType pdp2Type = getInternshipTypeByCode("PDP2", internshipTypes);
        if (pdp2Type != null) {
            List<InternshipDemand> pdp2Demands = demands.stream()
                    .filter(d -> d.getInternshipType().getId().equals(pdp2Type.getId()))
                    .collect(Collectors.toList());
            allocateTeachersForPDP(
                    allocationPlan,
                    teachers,
                    pdp2Demands,
                    teacherQualifications,
                    teacherExclusions,
                    teacherAvailabilities,
                    teacherSubjects,
                    zoneConstraints,
                    assignmentsCount,
                    assignedTypes,
                    combinationRules
            );
        }

        // Step 6: Handle any remaining teachers or unfilled demands
        handleRemainingTeachers(
                allocationPlan,
                teachers,
                demands,
                teacherQualifications,
                teacherExclusions,
                teacherAvailabilities,
                teacherSubjects,
                zoneConstraints,
                assignmentsCount,
                assignedTypes,
                combinationRules
        );

        // Step 7: Update CreditHourTracking for all teachers
        updateCreditHourTracking(allocationPlan, academicYear, assignmentsCount);

        // Step 8: Update the AllocationPlan status to 'COMPLETED' (using APPROVED as closest match)
        allocationPlan.setUpdatedAt(LocalDateTime.now());
        allocationPlan.setStatus(AllocationPlan.PlanStatus.APPROVED);
        entityManager.merge(allocationPlan);
        
        // Flush to ensure all assignments are persisted
        entityManager.flush();
        log.info("Allocation completed. Plan ID: {}, Total assignments created: {}", 
                allocationPlan.getId(), 
                entityManager.createQuery("SELECT COUNT(t) FROM TeacherAssignment t WHERE t.allocationPlan.id = :planId", Long.class)
                    .setParameter("planId", allocationPlan.getId())
                    .getSingleResult());

        return allocationPlan;
    }

    /**
     * Allocation Method 1: Allocates teachers for SFP (Study-accompanying subject-didactic internship).
     * This has the highest priority and strictest rules.
     * 
     * Logic:
     * - For each InternshipDemand where internshipType is 'SFP':
     *   - Find candidate teachers based on strict criteria
     *   - Sort candidates prioritizing teachers with subject as main_subject
     *   - Create TeacherAssignment records for top-ranked candidates
     *   - Create AllocationWarning if demand cannot be met
     * 
     * @param allocationPlan The allocation plan to add assignments to
     * @param teachers List of all available teachers
     * @param demands List of SFP internship demands
     * @param teacherQualifications Map of teacher qualifications
     * @param teacherExclusions Map of teacher subject exclusions
     * @param teacherAvailabilities Map of teacher availabilities
     * @param teacherSubjects Map of teacher subjects
     * @param zoneConstraints Map of zone constraints
     * @param assignmentsCount Map tracking assignment count per teacher
     * @param assignedTypes Map tracking assigned internship types per teacher
     * @param combinationRules Map of internship combination rules
     */
    private void allocateTeachersForSFP(
            AllocationPlan allocationPlan,
            List<Teacher> teachers,
            List<InternshipDemand> demands,
            Map<Long, List<TeacherQualification>> teacherQualifications,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Map<Long, List<TeacherSubject>> teacherSubjects,
            Map<Integer, List<ZoneConstraint>> zoneConstraints,
            Map<Teacher, Integer> assignmentsCount,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules
    ) {
        log.info("Processing {} SFP demands", demands.size());
        
        int totalAssignmentsCreated = 0;
        
        for (InternshipDemand demand : demands) {
            Subject requiredSubject = demand.getSubject();
            int requiredTeachers = demand.getRequiredTeachers();
            log.debug("Processing SFP demand - Subject: {}, Required Teachers: {}", 
                requiredSubject.getSubjectCode(), requiredTeachers);

            // ADD DIAGNOSTIC LOGGING: Count teachers at each filter stage
            long totalTeachers = teachers.size();
            long qualifiedForSubject = teachers.stream()
                .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                .count();
            long notExcluded = teachers.stream()
                .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                .count();
            long availableForSFP = teachers.stream()
                .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                .count();
            long lessThan2Assignments = teachers.stream()
                .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                .filter(t -> assignmentsCount.get(t) < 2)
                .count();
            long inAllowedZone = teachers.stream()
                .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                .filter(t -> assignmentsCount.get(t) < 2)
                .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                .count();
            long validCombination = teachers.stream()
                .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                .filter(t -> assignmentsCount.get(t) < 2)
                .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), assignedTypes, combinationRules))
                .count();
            
            log.warn("DIAGNOSTIC for Subject {}: Total={}, Qualified={}, NotExcluded={}, Available={}, <2Assignments={}, InZone={}, ValidCombo={}", 
                requiredSubject.getSubjectCode(), totalTeachers, qualifiedForSubject, notExcluded, 
                availableForSFP, lessThan2Assignments, inAllowedZone, validCombination);

            // Find a pool of candidate teachers by filtering based on criteria
                List<Teacher> candidateTeachers = teachers.stream()
                    .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                    .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                    .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                    .filter(t -> assignmentsCount.get(t) < 2)
                    .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                    .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), assignedTypes, combinationRules))
                    .collect(Collectors.toList());

            // Sort the candidates. Prioritize teachers who have the subject as a main_subject in their qualifications
            candidateTeachers.sort((t1, t2) -> {
                boolean t1HasMainSubject = hasMainSubjectQualification(t1, requiredSubject, teacherQualifications);
                boolean t2HasMainSubject = hasMainSubjectQualification(t2, requiredSubject, teacherQualifications);
                if (t1HasMainSubject && !t2HasMainSubject) {
                    return -1;
                } else if (!t1HasMainSubject && t2HasMainSubject) {
                    return 1;
                }
                TeacherAvailability.AvailabilityStatus t1Status = getAvailabilityStatus(t1, demand.getInternshipType(), teacherAvailabilities);
                TeacherAvailability.AvailabilityStatus t2Status = getAvailabilityStatus(t2, demand.getInternshipType(), teacherAvailabilities);
                if (t1Status == TeacherAvailability.AvailabilityStatus.PREFERRED && t2Status != TeacherAvailability.AvailabilityStatus.PREFERRED) {
                    return -1;
                } else if (t2Status == TeacherAvailability.AvailabilityStatus.PREFERRED && t1Status != TeacherAvailability.AvailabilityStatus.PREFERRED) {
                    return 1;
                }
                return 0;
            });

            log.debug("Found {} candidate teachers for SFP demand", candidateTeachers.size());
            
            // Create TeacherAssignment records for the top-ranked candidates until the demand's required_teachers count is met
            int assignedCount = 0;
            for (Teacher teacher : candidateTeachers) {
                if (assignedCount >= requiredTeachers) {
                    break;
                }

                TeacherAssignment assignment = new TeacherAssignment();
                assignment.setAllocationPlan(allocationPlan);
                assignment.setTeacher(teacher);
                assignment.setInternshipType(demand.getInternshipType());
                assignment.setSubject(requiredSubject);
                assignment.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
                assignment.setAssignedAt(LocalDateTime.now());
                
                // Set student group size based on demand
                if (demand.getStudentCount() != null && demand.getStudentCount() > 0) {
                    assignment.setStudentGroupSize(Math.max(1, demand.getStudentCount() / requiredTeachers));
                } else {
                    assignment.setStudentGroupSize(1);
                }

                entityManager.persist(assignment);
                log.info("Created SFP assignment - Teacher: {} {}, Subject: {}", 
                    teacher.getFirstName(), teacher.getLastName(), requiredSubject.getSubjectCode());

                // Update assignment count and types
                assignmentsCount.put(teacher, assignmentsCount.get(teacher) + 1);
                assignedTypes.get(teacher).add(demand.getInternshipType());
                assignedCount++;
                totalAssignmentsCreated++; // ADD THIS
            }

            // If the demand cannot be met, create an AllocationWarning to document the shortage
            if (assignedCount < requiredTeachers) {
                log.warn("Shortage for SFP demand - Subject: {}, Short by: {} teachers", 
                    requiredSubject.getSubjectCode(), requiredTeachers - assignedCount);
                createAllocationWarning(allocationPlan, demand, requiredTeachers - assignedCount);
            }
        }
        
        // ADD THIS: Log total assignments created
        log.info("SFP allocation completed. Total assignments created: {}", totalAssignmentsCreated);
    }

    /**
     * Allocation Method 2: Allocates teachers for ZSP (Additional study-accompanying internship).
     * This has medium priority and some flexibility.
     * 
     * Logic:
     * - Follow the same process as SFP to find teachers for the exact subject required
     * - If there is a shortage of teachers for the primary subject, attempt to find and assign
     *   teachers from alternative, related subjects
     * - When creating TeacherAssignment records, set the student_group_size to 2, reflecting
     *   the 1:2 teacher-to-student ratio for ZSP
     * 
     * @param allocationPlan The allocation plan to add assignments to
     * @param teachers List of all available teachers
     * @param demands List of ZSP internship demands
     * @param teacherQualifications Map of teacher qualifications
     * @param teacherExclusions Map of teacher subject exclusions
     * @param teacherAvailabilities Map of teacher availabilities
     * @param teacherSubjects Map of teacher subjects
     * @param zoneConstraints Map of zone constraints
     * @param assignmentsCount Map tracking assignment count per teacher
     * @param assignedTypes Map tracking assigned internship types per teacher
     * @param combinationRules Map of internship combination rules
     */
    private void allocateTeachersForZSP(
            AllocationPlan allocationPlan,
            List<Teacher> teachers,
            List<InternshipDemand> demands,
            Map<Long, List<TeacherQualification>> teacherQualifications,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Map<Long, List<TeacherSubject>> teacherSubjects,
            Map<Integer, List<ZoneConstraint>> zoneConstraints,
            Map<Teacher, Integer> assignmentsCount,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules
    ) {
        for (InternshipDemand demand : demands) {
            Subject primarySubject = demand.getSubject();
            int requiredTeachers = demand.getRequiredTeachers();

            // Find qualified teachers for this subject (same process as SFP)
            List<Teacher> qualifiedTeachers = teachers.stream()
                    .filter(t -> assignmentsCount.get(t) < 2)
                    .filter(t -> isTeacherQualifiedForSubject(t, primarySubject, teacherSubjects))
                    .filter(t -> !isTeacherExcludedFromSubject(t, primarySubject, teacherExclusions))
                    .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                    .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), assignedTypes, combinationRules))
                    .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                    .sorted((t1, t2) -> {
                        boolean t1HasMainSubject = hasMainSubjectQualification(t1, primarySubject, teacherQualifications);
                        boolean t2HasMainSubject = hasMainSubjectQualification(t2, primarySubject, teacherQualifications);
                        if (t1HasMainSubject && !t2HasMainSubject) {
                            return -1;
                        }
                        if (!t1HasMainSubject && t2HasMainSubject) {
                            return 1;
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());

            // Assign teachers up to the required number
            int assignedCount = 0;
            for (Teacher teacher : qualifiedTeachers) {
                if (assignedCount >= requiredTeachers) {
                    break;
                }

                TeacherAssignment assignment = new TeacherAssignment();
                assignment.setAllocationPlan(allocationPlan);
                assignment.setTeacher(teacher);
                assignment.setInternshipType(demand.getInternshipType());
                assignment.setSubject(primarySubject);
                assignment.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
                assignment.setAssignedAt(LocalDateTime.now());
                
                // Set student group size for ZSP (1:2 ratio)
                assignment.setStudentGroupSize(2);

                entityManager.persist(assignment);

                assignmentsCount.put(teacher, assignmentsCount.get(teacher) + 1);
                assignedTypes.get(teacher).add(demand.getInternshipType());
                assignedCount++;
            }

            // If there is a shortage of teachers for the primary subject, attempt to find and assign
            // teachers from alternative, related subjects
            if (assignedCount < requiredTeachers) {
                List<Subject> alternativeSubjects = findAlternativeSubjects(primarySubject);

                for (Subject alternativeSubject : alternativeSubjects) {
                    if (assignedCount >= requiredTeachers) {
                        break;
                    }

                        List<Teacher> alternativeTeachers = teachers.stream()
                            .filter(t -> assignmentsCount.get(t) < 2)
                            .filter(t -> isTeacherQualifiedForSubject(t, alternativeSubject, teacherSubjects))
                            .filter(t -> !isTeacherExcludedFromSubject(t, alternativeSubject, teacherExclusions))
                            .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                            .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), assignedTypes, combinationRules))
                            .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                            .collect(Collectors.toList());

                    for (Teacher teacher : alternativeTeachers) {
                        if (assignedCount >= requiredTeachers) {
                            break;
                        }

                        TeacherAssignment assignment = new TeacherAssignment();
                        assignment.setAllocationPlan(allocationPlan);
                        assignment.setTeacher(teacher);
                        assignment.setInternshipType(demand.getInternshipType());
                        assignment.setSubject(alternativeSubject);
                        assignment.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
                        assignment.setNotes("Alternative subject assignment for " + primarySubject.getSubjectTitle());
                        assignment.setAssignedAt(LocalDateTime.now());
                        
                        // Set student group size for ZSP (1:2 ratio)
                        assignment.setStudentGroupSize(2);

                        entityManager.persist(assignment);

                        assignmentsCount.put(teacher, assignmentsCount.get(teacher) + 1);
                        assignedTypes.get(teacher).add(demand.getInternshipType());
                        assignedCount++;
                    }
                }
            }

            // If we still couldn't meet the demand, create a warning
            if (assignedCount < requiredTeachers) {
                createAllocationWarning(allocationPlan, demand, requiredTeachers - assignedCount);
            }
        }
    }
    private void allocateTeachersForPDP(
            AllocationPlan allocationPlan,
            List<Teacher> teachers,
            List<InternshipDemand> demands,
            Map<Long, List<TeacherQualification>> teacherQualifications,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Map<Long, List<TeacherSubject>> teacherSubjects,
            Map<Integer, List<ZoneConstraint>> zoneConstraints,
            Map<Teacher, Integer> assignmentsCount,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules
    ) {
        for (InternshipDemand demand : demands) {
            Subject primarySubject = demand.getSubject();
            int requiredTeachers = demand.getRequiredTeachers();

            // Find teachers available for this internship type
            // Prioritize teachers from more distant zones (e.g., Zone 3) for block internships
            List<Teacher> availableTeachers = teachers.stream()
                    .filter(t -> assignmentsCount.get(t) < 2)
                    .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                    .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), assignedTypes, combinationRules))
                    .filter(t -> isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                    .sorted((t1, t2) -> {
                        int zone1 = t1.getSchool().getZoneNumber();
                        int zone2 = t2.getSchool().getZoneNumber();
                        return Integer.compare(zone2, zone1);
                    })
                    .collect(Collectors.toList());

            // Assign teachers up to the required number
            int assignedCount = 0;
            for (Teacher teacher : availableTeachers) {
                if (assignedCount >= requiredTeachers) {
                    break;
                }

                // Try to assign with primary subject if qualified
                Subject assignedSubject = primarySubject;
                if (!isTeacherQualifiedForSubject(teacher, primarySubject, teacherSubjects) ||
                    isTeacherExcludedFromSubject(teacher, primarySubject, teacherExclusions)) {
                    // Find any subject the teacher is qualified for
                    Optional<Subject> alternativeSubject = teacherSubjects.getOrDefault(teacher.getId(), Collections.emptyList())
                            .stream()
                            .filter(ts -> "Available".equals(ts.getAvailabilityStatus()) || 
                                         "Preferred".equals(ts.getAvailabilityStatus()))
                            .filter(ts -> !isTeacherExcludedFromSubject(teacher, ts.getSubject(), teacherExclusions))
                            .map(TeacherSubject::getSubject)
                            .findFirst();

                    if (alternativeSubject.isPresent()) {
                        assignedSubject = alternativeSubject.get();
                    } else {
                        continue; // Skip this teacher if no suitable subject found
                    }
                }
                }
            }
            }

            /**
             * Checks if a teacher is available for a given internship type.
             * @param teacher The teacher to check
             * @param internshipType The internship type to check
             * @param teacherAvailabilities Map of teacher availabilities
             * @return true if the teacher has marked themselves as 'AVAILABLE' or 'PREFERRED' for the internship type
             */
            private boolean isTeacherAvailableForInternship(
                Teacher teacher,
                InternshipType internshipType,
                Map<Long, List<TeacherAvailability>> teacherAvailabilities
            ) {
            List<TeacherAvailability> availabilities = teacherAvailabilities.getOrDefault(teacher.getId(), Collections.emptyList());
            return availabilities.stream()
                .anyMatch(a -> a.getInternshipType().getId().equals(internshipType.getId()) &&
                          (a.getStatus() == TeacherAvailability.AvailabilityStatus.AVAILABLE ||
                           a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED));
            }

    /**
     * Helper function: Updates credit hour tracking for all teachers.
     * If a teacher has exactly 2 assignments, they earn 1.0 credit hour.
     * 
     * @param allocationPlan The allocation plan
     * @param academicYear The academic year
     * @param assignmentsCount Map tracking assignment count per teacher
     */
    private void updateCreditHourTracking(
            AllocationPlan allocationPlan,
            AcademicYear academicYear,
            Map<Teacher, Integer> assignmentsCount
    ) {
        for (Map.Entry<Teacher, Integer> entry : assignmentsCount.entrySet()) {
            Teacher teacher = entry.getKey();
            int assignments = entry.getValue();

            // Calculate credit hours (1 credit hour for 2 assignments)
            double creditHours = assignments >= 2 ? 1.0 : 0.0;

            // Find existing credit hour tracking record
            CreditHourTracking tracking = entityManager.createQuery(
                            "SELECT c FROM CreditHourTracking c WHERE c.teacher.id = :teacherId AND c.academicYear.id = :academicYearId",
                            CreditHourTracking.class)
                    .setParameter("teacherId", teacher.getId())
                    .setParameter("academicYearId", academicYear.getId())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (tracking == null) {
                tracking = new CreditHourTracking();
                tracking.setTeacher(teacher);
                tracking.setAcademicYear(academicYear);
                tracking.setCreatedAt(LocalDateTime.now());
            }

            tracking.setAssignmentsCount(assignments);
            tracking.setCreditHoursAllocated(creditHours);
            tracking.setCreditBalance(0.0); // Assuming starting balance is 0
            tracking.setNotes(assignments >= 2 ? "Standard allocation met" : "Partial allocation");
            tracking.setUpdatedAt(LocalDateTime.now());

            entityManager.persist(tracking);
        }
    }

    /**
     * Helper function: Creates an allocation warning for unmet demand.
     * 
     * @param allocationPlan The allocation plan
     * @param demand The internship demand that couldn't be met
     * @param shortage The number of teachers short
     */
    private void createAllocationWarning(AllocationPlan allocationPlan, InternshipDemand demand, int shortage) {
        AllocationWarning warning = new AllocationWarning();
        warning.setAllocationPlan(allocationPlan);
        warning.setInternshipType(demand.getInternshipType());
        warning.setSubject(demand.getSubject());
        warning.setSchoolType(demand.getSchoolType().toString());
        warning.setShortage(shortage);
        warning.setWarningType(AllocationWarning.WarningType.TEACHER_SHORTAGE);
        warning.setMessage("Shortage of " + shortage + " teachers for " +
                demand.getInternshipType().getFullName() + " in " +
                demand.getSubject().getSubjectTitle());
        warning.setCreatedAt(LocalDateTime.now());
        entityManager.persist(warning);
    }

    /**
     * Helper function: Finds alternative subjects related to the primary subject.
     * 
     * @param primarySubject The primary subject
     * @return List of alternative subjects
     */
    private List<Subject> findAlternativeSubjects(Subject primarySubject) {
        // This is a simplified implementation
        // In a real system, this would look up related subjects from the same category
        List<Subject> alternatives = entityManager.createQuery(
                        "SELECT s FROM Subject s WHERE s.subjectCategory.id = :categoryId AND s.id != :subjectId",
                        Subject.class)
                .setParameter("categoryId", primarySubject.getSubjectCategory().getId())
                .setParameter("subjectId", primarySubject.getId())
                .getResultList();
        return alternatives;
    }

    /**
     * Helper function: Handles remaining teachers for any unmet demands.
     * 
     * @param allocationPlan The allocation plan
     * @param teachers List of all teachers
     * @param demands List of all demands
     * @param teacherQualifications Map of teacher qualifications
     * @param teacherExclusions Map of teacher exclusions
     * @param teacherAvailabilities Map of teacher availabilities
     * @param teacherSubjects Map of teacher subjects
     * @param zoneConstraints Map of zone constraints
     * @param assignmentsCount Map tracking assignment count per teacher
     * @param assignedTypes Map tracking assigned internship types per teacher
     * @param combinationRules Map of internship combination rules
     */
    private void handleRemainingTeachers(
            AllocationPlan allocationPlan,
            List<Teacher> teachers,
            List<InternshipDemand> demands,
            Map<Long, List<TeacherQualification>> teacherQualifications,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Map<Long, List<TeacherSubject>> teacherSubjects,
            Map<Integer, List<ZoneConstraint>> zoneConstraints,
            Map<Teacher, Integer> assignmentsCount,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules
    ) {
        // This method can be extended to handle edge cases where teachers remain unassigned
        // or demands remain unfilled after the main allocation process
        // For now, it's a placeholder for future enhancements
    }

    /**
     * Helper function: Checks if a teacher has a main subject qualification.
     * 
     * @param teacher The teacher to check
     * @param subject The subject to check
     * @param teacherQualifications Map of teacher qualifications
     * @return true if the teacher has the subject as a main subject
     */
    private boolean hasMainSubjectQualification(
            Teacher teacher,
            Subject subject,
            Map<Long, List<TeacherQualification>> teacherQualifications
    ) {
        List<TeacherQualification> qualifications = teacherQualifications.getOrDefault(teacher.getId(), Collections.emptyList());
        return qualifications.stream()
                .anyMatch(q -> q.getSubject().getId().equals(subject.getId()) &&
                             Boolean.TRUE.equals(q.getIsMainSubject()));
    }

    /**
     * Helper function: Gets the availability status for a teacher and internship type.
     * 
     * @param teacher The teacher
     * @param internshipType The internship type
     * @param teacherAvailabilities Map of teacher availabilities
     * @return The availability status
     */
    private TeacherAvailability.AvailabilityStatus getAvailabilityStatus(
            Teacher teacher,
            InternshipType internshipType,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities
    ) {
        List<TeacherAvailability> availabilities = teacherAvailabilities.getOrDefault(teacher.getId(), Collections.emptyList());
        return availabilities.stream()
                .filter(a -> a.getInternshipType().getId().equals(internshipType.getId()))
                .map(TeacherAvailability::getStatus)
                .findFirst()
                .orElse(TeacherAvailability.AvailabilityStatus.NOT_AVAILABLE);
    }

    /**
     * Gets an internship type by its code.
     * 
     * @param code The internship code (e.g., "SFP", "ZSP", "PDP1", "PDP2")
     * @param internshipTypes List of all internship types
     * @return The internship type matching the code, or null if not found
     */
    private InternshipType getInternshipTypeByCode(String code, List<InternshipType> internshipTypes) {
        return internshipTypes.stream()
                .filter(it -> it.getInternshipCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generates the next unique version number for an allocation plan in the given academic year.
     * Queries existing plans to find the highest version and increments it.
     * 
     * @param academicYearId The academic year ID
     * @return A unique version string (e.g., "1.0", "2.0", "3.0")
     */
    private String generateNextVersion(Long academicYearId) {
        List<AllocationPlan> existingPlans = entityManager.createQuery(
                "SELECT ap FROM AllocationPlan ap WHERE ap.academicYear.id = :yearId ORDER BY ap.id DESC",
                AllocationPlan.class)
                .setParameter("yearId", academicYearId)
                .getResultList();

        if (existingPlans.isEmpty()) {
            return "1.0";
        }

        // Find the highest version number
        int maxVersion = 1;
        for (AllocationPlan plan : existingPlans) {
            String version = plan.getPlanVersion();
            if (version != null && version.matches("\\d+\\.\\d+")) {
                try {
                    int majorVersion = Integer.parseInt(version.split("\\.")[0]);
                    maxVersion = Math.max(maxVersion, majorVersion);
                } catch (NumberFormatException e) {
                    // Skip versions that don't follow major.minor pattern
                }
            }
        }

        return (maxVersion + 1) + ".0";
    }
}

