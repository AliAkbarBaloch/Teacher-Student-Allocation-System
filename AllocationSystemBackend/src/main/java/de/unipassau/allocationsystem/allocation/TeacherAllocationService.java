package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
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
@Service
public class TeacherAllocationService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Main orchestration method that orchestrates the entire allocation process.
     * 
     * @param academicYearId The ID of the academic year for allocation
     * @return AllocationPlan containing all teacher assignments
     * @throws IllegalArgumentException if the academic year is invalid or locked
     */
    @Transactional
    public AllocationPlan performAllocation(Long academicYearId) {
        // Step 1: Validate that the AcademicYear exists and is not locked
        AcademicYear academicYear = entityManager.find(AcademicYear.class, academicYearId);
        if (academicYear == null) {
            throw new IllegalArgumentException("Academic year with ID " + academicYearId + " not found");
        }
        if (Boolean.TRUE.equals(academicYear.getIsLocked())) {
            throw new IllegalArgumentException("Academic year is locked and cannot be modified");
        }

        // Step 2: Create a new AllocationPlan entity with status 'DRAFT'
        AllocationPlan allocationPlan = new AllocationPlan();
        allocationPlan.setAcademicYear(academicYear);
        allocationPlan.setPlanName("Allocation Plan for " + academicYear.getYearName());
        allocationPlan.setPlanVersion("1.0");
        allocationPlan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        allocationPlan.setIsCurrent(false);
        allocationPlan.setCreatedAt(LocalDateTime.now());
        allocationPlan.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(allocationPlan);

        // Step 3: Load all necessary data from the database into memory for efficient processing
        List<Teacher> teachers = loadAvailableTeachers(academicYearId);
        List<InternshipDemand> demands = loadInternshipDemands(academicYearId);
        Map<Long, List<TeacherQualification>> teacherQualifications = loadTeacherQualifications();
        Map<Long, List<TeacherSubjectExclusion>> teacherExclusions = loadTeacherExclusions(academicYearId);
        Map<Long, List<TeacherAvailability>> teacherAvailabilities = loadTeacherAvailabilities(academicYearId);
        Map<Long, List<TeacherSubject>> teacherSubjects = loadTeacherSubjects(academicYearId);
        List<InternshipType> internshipTypes = loadInternshipTypes();
        List<School> schools = loadSchools();
        Map<Integer, List<ZoneConstraint>> zoneConstraints = loadZoneConstraints();
        Map<Long, List<InternshipCombinationRule>> combinationRules = loadCombinationRules();

        // Step 4: Initialize tracking structures to monitor the number of assignments per teacher
        Map<Teacher, Integer> assignmentsCount = new HashMap<>();
        Map<Teacher, List<InternshipType>> assignedTypes = new HashMap<>();
        for (Teacher teacher : teachers) {
            assignmentsCount.put(teacher, 0);
            assignedTypes.put(teacher, new ArrayList<>());
        }

        // Step 5: Execute the allocation methods in strict priority order
        // First: SFP allocation (Priority 1)
        InternshipType sfpType = getInternshipTypeByCode("SFP", internshipTypes);
        if (sfpType != null) {
            List<InternshipDemand> sfpDemands = demands.stream()
                    .filter(d -> d.getInternshipType().getId().equals(sfpType.getId()))
                    .collect(Collectors.toList());
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
        for (InternshipDemand demand : demands) {
            Subject requiredSubject = demand.getSubject();
            int requiredTeachers = demand.getRequiredTeachers();

            // Find a pool of candidate teachers by filtering based on criteria
            List<Teacher> candidateTeachers = teachers.stream()
                    // Teacher is qualified for the exact Subject of the demand
                    .filter(t -> isTeacherQualifiedForSubject(t, requiredSubject, teacherSubjects))
                    // Teacher is not excluded from teaching that subject this year
                    .filter(t -> !isTeacherExcludedFromSubject(t, requiredSubject, teacherExclusions))
                    // Teacher has marked themselves as 'AVAILABLE' or 'PREFERRED' for SFP
                    .filter(t -> isTeacherAvailableForInternship(t, demand.getInternshipType(), teacherAvailabilities))
                    // Teacher has fewer than 2 total assignments
                    .filter(t -> assignmentsCount.get(t) < 2)
                    // Teacher's school zoneNumber is permitted for SFP according to ZoneConstraint rules
                    .filter(t -> isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                    // The SFP internship forms a valid combination with any other internship the teacher is already assigned to
                    .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), assignedTypes, combinationRules))
                    .collect(Collectors.toList());

            // Sort the candidates. Prioritize teachers who have the subject as a main_subject in their qualifications
            candidateTeachers.sort((t1, t2) -> {
                boolean t1HasMainSubject = hasMainSubjectQualification(t1, requiredSubject, teacherQualifications);
                boolean t2HasMainSubject = hasMainSubjectQualification(t2, requiredSubject, teacherQualifications);
                
                if (t1HasMainSubject && !t2HasMainSubject) {
                    return -1; // t1 has main subject, prioritize
                } else if (!t1HasMainSubject && t2HasMainSubject) {
                    return 1; // t2 has main subject, prioritize
                }
                
                // If both or neither have main subject, prefer teachers with PREFERRED status
                TeacherAvailability.AvailabilityStatus t1Status = getAvailabilityStatus(t1, demand.getInternshipType(), teacherAvailabilities);
                TeacherAvailability.AvailabilityStatus t2Status = getAvailabilityStatus(t2, demand.getInternshipType(), teacherAvailabilities);
                
                if (t1Status == TeacherAvailability.AvailabilityStatus.PREFERRED && 
                    t2Status != TeacherAvailability.AvailabilityStatus.PREFERRED) {
                    return -1;
                } else if (t2Status == TeacherAvailability.AvailabilityStatus.PREFERRED && 
                          t1Status != TeacherAvailability.AvailabilityStatus.PREFERRED) {
                    return 1;
                }
                
                return 0; // Equal priority
            });

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

                // Update assignment count and types
                assignmentsCount.put(teacher, assignmentsCount.get(teacher) + 1);
                assignedTypes.get(teacher).add(demand.getInternshipType());
                assignedCount++;
            }

            // If the demand cannot be met, create an AllocationWarning to document the shortage
            if (assignedCount < requiredTeachers) {
                createAllocationWarning(allocationPlan, demand, requiredTeachers - assignedCount);
            }
        }
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
                    .filter(t -> isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
                    .sorted((t1, t2) -> {
                        boolean t1HasMainSubject = hasMainSubjectQualification(t1, primarySubject, teacherQualifications);
                        boolean t2HasMainSubject = hasMainSubjectQualification(t2, primarySubject, teacherQualifications);
                        if (t1HasMainSubject && !t2HasMainSubject) return -1;
                        if (!t1HasMainSubject && t2HasMainSubject) return 1;
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
                            .filter(t -> isTeacherInAllowedZone(t, demand.getInternshipType(), zoneConstraints))
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

    /**
     * Allocation Method 3: Allocates teachers for PDP1/PDP2 (Block internships).
     * This has the lowest priority, and subject matching is flexible.
     * 
     * Logic:
     * - The primary filter should be the teacher's school zoneNumber
     * - Prioritize teachers from more distant zones (e.g., Zone 3) for block internships
     * - If a teacher is available and in a suitable zone, assign them to the PDP internship
     *   even if they are not qualified for the placeholder subject in the demand
     * - Find any subject they are qualified for and make the assignment
     * - When creating TeacherAssignment records, set the student_group_size to 1, reflecting
     *   the 1:1 supervision model for block internships
     * 
     * @param allocationPlan The allocation plan to add assignments to
     * @param teachers List of all available teachers
     * @param demands List of PDP internship demands
     * @param teacherQualifications Map of teacher qualifications
     * @param teacherExclusions Map of teacher subject exclusions
     * @param teacherAvailabilities Map of teacher availabilities
     * @param teacherSubjects Map of teacher subjects
     * @param zoneConstraints Map of zone constraints
     * @param assignmentsCount Map tracking assignment count per teacher
     * @param assignedTypes Map tracking assigned internship types per teacher
     * @param combinationRules Map of internship combination rules
     */
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
                        // Prioritize teachers from more distant zones (higher zone number)
                        int zone1 = t1.getSchool().getZoneNumber();
                        int zone2 = t2.getSchool().getZoneNumber();
                        return Integer.compare(zone2, zone1); // Higher zone number first
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

                TeacherAssignment assignment = new TeacherAssignment();
                assignment.setAllocationPlan(allocationPlan);
                assignment.setTeacher(teacher);
                assignment.setInternshipType(demand.getInternshipType());
                assignment.setSubject(assignedSubject);
                assignment.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
                if (!assignedSubject.equals(primarySubject)) {
                    assignment.setNotes("Alternative subject assignment for " + primarySubject.getSubjectTitle());
                }
                assignment.setAssignedAt(LocalDateTime.now());
                
                // Set student group size for PDP (1:1 ratio)
                assignment.setStudentGroupSize(1);

                entityManager.persist(assignment);

                assignmentsCount.put(teacher, assignmentsCount.get(teacher) + 1);
                assignedTypes.get(teacher).add(demand.getInternshipType());
                assignedCount++;
            }

            // If we couldn't meet the demand, create a warning
            if (assignedCount < requiredTeachers) {
                createAllocationWarning(allocationPlan, demand, requiredTeachers - assignedCount);
            }
        }
    }

    /**
     * Helper function: Checks if a teacher is in an allowed zone for the internship type.
     * 
     * @param teacher The teacher to check
     * @param internshipType The internship type
     * @param zoneConstraints Map of zone constraints
     * @return true if the teacher's school zone is permitted for the internship type
     */
    private boolean isTeacherInAllowedZone(
            Teacher teacher,
            InternshipType internshipType,
            Map<Integer, List<ZoneConstraint>> zoneConstraints
    ) {
        int teacherZone = teacher.getSchool().getZoneNumber();
        List<ZoneConstraint> constraints = zoneConstraints.getOrDefault(teacherZone, Collections.emptyList());

        return constraints.stream()
                .anyMatch(c -> c.getInternshipType().getId().equals(internshipType.getId()) && 
                              Boolean.TRUE.equals(c.getIsAllowed()));
    }

    /**
     * Helper function: Checks if a teacher can be assigned to an internship type based on combination rules.
     * 
     * @param teacher The teacher to check
     * @param internshipType The new internship type to assign
     * @param assignedTypes Map of currently assigned internship types per teacher
     * @param combinationRules Map of internship combination rules
     * @return true if adding the new internship type results in a valid combination
     */
    private boolean canTeacherBeAssignedToInternship(
            Teacher teacher,
            InternshipType internshipType,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules
    ) {
        List<InternshipType> currentAssignments = assignedTypes.get(teacher);

        // If teacher has no assignments yet, any internship type is allowed
        if (currentAssignments.isEmpty()) {
            return true;
        }

        // Check if the new internship type can be combined with existing assignments
        for (InternshipType existingType : currentAssignments) {
            List<InternshipCombinationRule> rules = combinationRules.getOrDefault(existingType.getId(), Collections.emptyList());
            boolean isValidCombination = rules.stream()
                    .anyMatch(rule -> rule.getInternshipType2().getId().equals(internshipType.getId()) &&
                            Boolean.TRUE.equals(rule.getIsValidCombination()));

            if (!isValidCombination) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper function: Checks if a teacher is qualified for a subject.
     * 
     * @param teacher The teacher to check
     * @param subject The subject to check
     * @param teacherSubjects Map of teacher subjects
     * @return true if the teacher has a TeacherQualification for the subject and no TeacherSubjectExclusion
     */
    private boolean isTeacherQualifiedForSubject(
            Teacher teacher,
            Subject subject,
            Map<Long, List<TeacherSubject>> teacherSubjects
    ) {
        List<TeacherSubject> subjects = teacherSubjects.getOrDefault(teacher.getId(), Collections.emptyList());
        return subjects.stream()
                .anyMatch(ts -> ts.getSubject().getId().equals(subject.getId()) &&
                              ("Available".equals(ts.getAvailabilityStatus()) || 
                               "Preferred".equals(ts.getAvailabilityStatus())));
    }

    /**
     * Helper function: Checks if a teacher is excluded from teaching a subject.
     * 
     * @param teacher The teacher to check
     * @param subject The subject to check
     * @param teacherExclusions Map of teacher subject exclusions
     * @return true if the teacher has a TeacherSubjectExclusion for the subject
     */
    private boolean isTeacherExcludedFromSubject(
            Teacher teacher,
            Subject subject,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions
    ) {
        List<TeacherSubjectExclusion> exclusions = teacherExclusions.getOrDefault(teacher.getId(), Collections.emptyList());
        return exclusions.stream()
                .anyMatch(e -> e.getSubject().getId().equals(subject.getId()));
    }

    /**
     * Helper function: Checks if a teacher is available for an internship type.
     * 
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

    // Data loading methods

    /**
     * Loads all available teachers for the academic year.
     */
    private List<Teacher> loadAvailableTeachers(Long academicYearId) {
        return entityManager.createQuery(
                        "SELECT t FROM Teacher t WHERE t.isActive = true AND t.employmentStatus = :status",
                        Teacher.class)
                .setParameter("status", Teacher.EmploymentStatus.ACTIVE)
                .getResultList();
    }

    /**
     * Loads all internship demands for the academic year.
     */
    private List<InternshipDemand> loadInternshipDemands(Long academicYearId) {
        return entityManager.createQuery(
                        "SELECT d FROM InternshipDemand d WHERE d.academicYear.id = :academicYearId",
                        InternshipDemand.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();
    }

    /**
     * Loads all teacher qualifications.
     */
    private Map<Long, List<TeacherQualification>> loadTeacherQualifications() {
        List<TeacherQualification> qualifications = entityManager.createQuery(
                        "SELECT q FROM TeacherQualification q",
                        TeacherQualification.class)
                .getResultList();

        return qualifications.stream()
                .collect(Collectors.groupingBy(q -> q.getTeacher().getId()));
    }

    /**
     * Loads all teacher exclusions for the academic year.
     */
    private Map<Long, List<TeacherSubjectExclusion>> loadTeacherExclusions(Long academicYearId) {
        List<TeacherSubjectExclusion> exclusions = entityManager.createQuery(
                        "SELECT e FROM TeacherSubjectExclusion e WHERE e.academicYear.id = :academicYearId",
                        TeacherSubjectExclusion.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();

        return exclusions.stream()
                .collect(Collectors.groupingBy(e -> e.getTeacher().getId()));
    }

    /**
     * Loads all teacher availabilities for the academic year.
     */
    private Map<Long, List<TeacherAvailability>> loadTeacherAvailabilities(Long academicYearId) {
        List<TeacherAvailability> availabilities = entityManager.createQuery(
                        "SELECT a FROM TeacherAvailability a WHERE a.academicYear.id = :academicYearId",
                        TeacherAvailability.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();

        return availabilities.stream()
                .collect(Collectors.groupingBy(a -> a.getTeacher().getId()));
    }

    /**
     * Loads all teacher subjects for the academic year.
     */
    private Map<Long, List<TeacherSubject>> loadTeacherSubjects(Long academicYearId) {
        List<TeacherSubject> subjects = entityManager.createQuery(
                        "SELECT ts FROM TeacherSubject ts WHERE ts.academicYear.id = :academicYearId",
                        TeacherSubject.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();

        return subjects.stream()
                .collect(Collectors.groupingBy(ts -> ts.getTeacher().getId()));
    }

    /**
     * Loads all internship types.
     */
    private List<InternshipType> loadInternshipTypes() {
        return entityManager.createQuery(
                        "SELECT it FROM InternshipType it",
                        InternshipType.class)
                .getResultList();
    }

    /**
     * Loads all schools.
     */
    private List<School> loadSchools() {
        return entityManager.createQuery(
                        "SELECT s FROM School s WHERE s.isActive = true",
                        School.class)
                .getResultList();
    }

    /**
     * Loads all zone constraints.
     */
    private Map<Integer, List<ZoneConstraint>> loadZoneConstraints() {
        List<ZoneConstraint> constraints = entityManager.createQuery(
                        "SELECT z FROM ZoneConstraint z",
                        ZoneConstraint.class)
                .getResultList();

        return constraints.stream()
                .collect(Collectors.groupingBy(ZoneConstraint::getZoneNumber));
    }

    /**
     * Loads all internship combination rules.
     */
    private Map<Long, List<InternshipCombinationRule>> loadCombinationRules() {
        List<InternshipCombinationRule> rules = entityManager.createQuery(
                        "SELECT r FROM InternshipCombinationRule r",
                        InternshipCombinationRule.class)
                .getResultList();

        return rules.stream()
                .collect(Collectors.groupingBy(r -> r.getInternshipType1().getId()));
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
}

