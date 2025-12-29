package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.dto.allocation.AllocationParameters;
import de.unipassau.allocationsystem.entity.*;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ImprovedTeacherAllocationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AllocationDataLoader dataLoader;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    @Transactional
    public AllocationPlan performAllocation(Long academicYearId, AllocationParameters params) {
        log.info("=== Starting Allocation for Year ID: {} ===", academicYearId);

        AcademicYear year = entityManager.find(AcademicYear.class, academicYearId);
        if (year == null) {
            throw new IllegalArgumentException("Year not found: " + academicYearId);
        }

        AllocationContext ctx = buildContext(academicYearId, params);
        AllocationPlan plan = createDraftPlan(year);

        // --- PHASE 1: DEMAND DRIVEN ---
        calculateScarcityMetrics(ctx);
        allocateByPriority(plan, ctx, "SFP");
        allocateByPriority(plan, ctx, "ZSP");
        allocateByPriority(plan, ctx, "PDP1");
        allocateByPriority(plan, ctx, "PDP2");

        log.info("Phase 1 Complete. Assignments: {}", ctx.totalAssignmentsCreated);

        // --- PHASE 2: SUPPLY DRIVEN (Fixing the 150/28 issue) ---
        if (params.isForceUtilizationOfSurplus()) {
            handleSurplusTeachers(plan, ctx);
        }

        // --- PHASE 3: VALIDATION & FINALIZATION ---
        validateBudget(ctx, year);
        finalizePlan(plan, year);

        return plan;
    }

    // ============================================================================================
    // PHASE 2: SURPLUS HANDLING (With Duplicate Prevention)
    // ============================================================================================

    private void handleSurplusTeachers(AllocationPlan plan, AllocationContext ctx) {
        List<Teacher> underutilized = ctx.teachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) < ctx.getTargetAssignments(t))
                .collect(Collectors.toList());

        log.info("Phase 2: Processing {} underutilized teachers.", underutilized.size());

        InternshipType pdp1 = ctx.getInternshipType("PDP1");
        InternshipType pdp2 = ctx.getInternshipType("PDP2");
        InternshipType zsp = ctx.getInternshipType("ZSP");
        InternshipType sfp = ctx.getInternshipType("SFP");

        for (Teacher t : underutilized) {
            int zone = t.getSchool().getZoneNumber();
            int attempts = 0; // Safety break

            while (ctx.getAssignmentCount(t) < ctx.getTargetAssignments(t) && attempts < 10) {
                boolean assigned = false;
                attempts++;

                // Strategy: Try Zone-Preferred types first
                if (zone == 3) {
                    if (tryForceAssign(plan, ctx, t, pdp1)) {
                        assigned = true;
                    } else if (tryForceAssign(plan, ctx, t, pdp2)) {
                        assigned = true;
                    }
                } else {
                    // Zone 1/2
                    if (tryForceAssign(plan, ctx, t, zsp)) {
                        assigned = true;
                    } else if (tryForceAssign(plan, ctx, t, sfp)) {
                        assigned = true;
                    }  else if (tryForceAssign(plan, ctx, t, pdp1)) {
                        assigned = true;
                    }  else if (tryForceAssign(plan, ctx, t, pdp2)) {
                        assigned = true;
                    }
                }

                if (!assigned) {
                    // Final Resort: Ignore Zone Constraints if absolutely necessary to hit budget?
                    // For now, we log and break to prevent infinite loops or constraint violations
                    log.warn("Cannot force assignment for Teacher {} (Zone {}) despite being underutilized. Check Qualification/Zone rules.", t.getId(), zone);
                    break;
                }
            }
        }
    }

    private boolean tryForceAssign(AllocationPlan plan, AllocationContext ctx, Teacher t, InternshipType type) {
        if (type == null) {
            return false;
        }

        // 1. Check Hard Constraints
        if (!AllocationHelper.isTeacherInAllowedZone(t, type, ctx.zoneConstraints)) {
            return false;
        }
        if (!AllocationHelper.canTeacherBeAssignedToInternship(t, type, ctx.assignedTypes, ctx.combinationRules)) {
            return false;
        }

        // 2. Pick a Subject (CRITICAL FIX: Don't pick one they already have for this type!)
        Subject subject = findBestSubjectForSurplus(ctx, t, type);

        if (subject == null) {
            return false; // No valid unique subject found
        }

        // 3. Create Assignment
        createAssignment(plan, ctx, t, type, subject, "Forced Surplus Allocation");
        return true;
    }

    private Subject findBestSubjectForSurplus(AllocationContext ctx, Teacher t, InternshipType type) {
        // First, try qualifications
        List<TeacherQualification> quals = ctx.qualifications.getOrDefault(t.getId(), Collections.emptyList());
        for (TeacherQualification q : quals) {
            Subject s = q.getSubject();
            if (!ctx.hasAssignment(t, type, s)) {
                return s;
            }
        }

        // Fallback: If qualifications are exhausted (or empty), pick a generic subject based on school type
        // BUT ensure it's not already assigned to this teacher for this type
        Subject fallback = ctx.getFallbackSubject(t.getSchool().getSchoolType().toString());
        if (fallback != null && !ctx.hasAssignment(t, type, fallback)) {
            return fallback;
        }

        return null; // Cannot find a valid unique subject
    }

    // ============================================================================================
    // PERSISTENCE & CONTEXT UPDATE (With Check)
    // ============================================================================================

    private void createAssignment(AllocationPlan plan, AllocationContext ctx, Teacher t, InternshipType type, Subject s, String note) {
        // FINAL SAFETY CHECK: Duplicate Prevention
        if (ctx.hasAssignment(t, type, s)) {
            log.warn("Skipping duplicate assignment attempt: Teacher {} -> {} - {}", t.getId(), type.getInternshipCode(), s.getSubjectCode());
            return;
        }

        TeacherAssignment ta = new TeacherAssignment();
        ta.setAllocationPlan(plan);
        ta.setTeacher(t);
        ta.setInternshipType(type);
        ta.setSubject(s);
        ta.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
        ta.setAssignedAt(LocalDateTime.now());
        ta.setNotes(note);
        ta.setStudentGroupSize(1);

        teacherAssignmentRepository.save(ta);
        ctx.recordAssignment(t, type, s); // Updates counters AND unique tracking set
    }

    // ============================================================================================
    // PHASE 1: DEMAND DRIVEN LOGIC
    // ============================================================================================

    private void allocateByPriority(AllocationPlan plan, AllocationContext ctx, String typeCode) {
        InternshipType type = ctx.getInternshipType(typeCode);
        if (type == null) {
            return;
        }
        List<InternshipDemand> typeDemands = ctx.getDemandsByType(type.getId());

        if (ctx.params.isPrioritizeScarcity()) {
            typeDemands.sort(Comparator.comparingInt(d -> ctx.getCandidateCountForSubject(d.getSubject().getId())));
        }

        for (InternshipDemand demand : typeDemands) {
            processDemand(plan, ctx, demand);
        }
    }

    private void processDemand(AllocationPlan plan, AllocationContext ctx, InternshipDemand demand) {
        int required = demand.getRequiredTeachers();
        List<Teacher> candidates = findCandidates(ctx, demand);
        candidates.sort((t1, t2) -> Integer.compare(scoreTeacher(t2, demand, ctx), scoreTeacher(t1, demand, ctx)));

        int assigned = 0;
        for (Teacher teacher : candidates) {
            if (assigned >= required) {
                break;
            }
            // Check for duplicate specific assignment before proceeding
            if (!ctx.hasAssignment(teacher, demand.getInternshipType(), demand.getSubject())) {
                createAssignment(plan, ctx, teacher, demand.getInternshipType(), demand.getSubject(), "Demand Match");
                assigned++;
            }
        }
    }

    private List<Teacher> findCandidates(AllocationContext ctx, InternshipDemand demand) {
        return ctx.teachers.stream()
                .filter(t -> !ctx.isTeacherFullyBooked(t))
                .filter(t -> {
                    if (!demand.getInternshipType().getIsSubjectSpecific()) {
                        return true;
                    }
                    return AllocationHelper.isTeacherQualifiedForSubject(t, demand.getSubject(), ctx.teacherSubjects);
                })
                .filter(t -> !AllocationHelper.isTeacherExcludedFromSubject(t, demand.getSubject(), ctx.exclusions))
                .filter(t -> AllocationHelper.isTeacherAvailableForInternship(t, demand.getInternshipType(), ctx.availabilities))
                .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), ctx.zoneConstraints))
                .filter(t -> AllocationHelper.canTeacherBeAssignedToInternship(t, demand.getInternshipType(), ctx.assignedTypes, ctx.combinationRules))
                .collect(Collectors.toList());
    }

    private int scoreTeacher(Teacher t, InternshipDemand d, AllocationContext ctx) {
        int score = 0;
        if (AllocationHelper.hasMainSubjectQualification(t, d.getSubject(), ctx.qualifications)) {
            score += 10;
        }

        boolean isWednesday = d.getInternshipType().getInternshipCode().equals("ZSP") ||
                d.getInternshipType().getInternshipCode().equals("SFP");
        if (isWednesday && t.getSchool().getZoneNumber() == 1) {
            score += 5;
        }
        if (ctx.getAssignmentCount(t) == 0) {
            score += 50;
        }
        return score;
    }

    // ============================================================================================
    // CONTEXT, BUILDER, VALIDATION
    // ============================================================================================

    private void calculateScarcityMetrics(AllocationContext ctx) {
        for (Teacher t : ctx.teachers) {
            List<TeacherSubject> subs = ctx.teacherSubjects.get(t.getId());
            if (subs != null) {
                for (TeacherSubject ts : subs) {
                    ctx.incrementCandidateCount(ts.getSubject().getId());
                }
            }
        }
    }

    private void validateBudget(AllocationContext ctx, AcademicYear year) {
        long primaryFilled = ctx.teachers.stream()
                .filter(t -> t.getSchool().getSchoolType() == School.SchoolType.PRIMARY)
                .filter(t -> ctx.getAssignmentCount(t) >= 2).count();
        long middleFilled = ctx.teachers.stream()
                .filter(t -> t.getSchool().getSchoolType() == School.SchoolType.MIDDLE)
                .filter(t -> ctx.getAssignmentCount(t) >= 2).count();

        log.info("BUDGET: Primary {}/{}, Middle {}/{}", primaryFilled, year.getElementarySchoolHours(), middleFilled, year.getMiddleSchoolHours());
    }

    private AllocationPlan createDraftPlan(AcademicYear year) {
        AllocationPlan plan = new AllocationPlan();
        plan.setAcademicYear(year);
        plan.setPlanName("Auto-Plan " + LocalDateTime.now());
        plan.setPlanVersion(UUID.randomUUID().toString().substring(0, 8));
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan.setIsCurrent(false);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(plan);
        return plan;
    }

    private void finalizePlan(AllocationPlan plan, AcademicYear year) {
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan.setUpdatedAt(LocalDateTime.now());
        entityManager.merge(plan);
        entityManager.flush();
    }

    private AllocationContext buildContext(Long yearId, AllocationParameters params) {
        AllocationContext ctx = new AllocationContext(params);
        ctx.teachers = dataLoader.loadAvailableTeachers(yearId);
        ctx.demands = dataLoader.loadInternshipDemands(yearId);
        ctx.qualifications = dataLoader.loadTeacherQualifications();
        ctx.exclusions = dataLoader.loadTeacherExclusions(yearId);
        ctx.availabilities = dataLoader.loadTeacherAvailabilities(yearId);
        ctx.teacherSubjects = dataLoader.loadTeacherSubjects(yearId);
        ctx.internshipTypes = dataLoader.loadInternshipTypes();
        ctx.zoneConstraints = dataLoader.loadZoneConstraints();
        ctx.combinationRules = dataLoader.loadCombinationRules();

        List<Subject> allSubjects = entityManager.createQuery("SELECT s FROM Subject s", Subject.class).getResultList();
        ctx.fallbackSubjects.put("PRIMARY", allSubjects.stream().filter(s -> "Primary".equalsIgnoreCase(s.getSchoolType())).findFirst().orElse(null));
        ctx.fallbackSubjects.put("MIDDLE", allSubjects.stream().filter(s -> "Middle".equalsIgnoreCase(s.getSchoolType())).findFirst().orElse(null));

        for (Teacher t : ctx.teachers) {
            ctx.currentAssignmentCount.put(t.getId(), 0);
            ctx.assignedTypes.put(t, new ArrayList<>());
            // Initialize empty set for unique key tracking
            ctx.uniqueAssignments.put(t.getId(), new HashSet<>());
        }
        return ctx;
    }

    // ============================================================================================
    // INNER CLASS: AllocationContext
    // ============================================================================================
    private static class AllocationContext {
        private AllocationParameters params;
        private List<Teacher> teachers;
        private List<InternshipDemand> demands;

        private Map<Long, List<TeacherQualification>> qualifications;
        private Map<Long, List<TeacherSubjectExclusion>> exclusions;
        private Map<Long, List<TeacherAvailability>> availabilities;
        private Map<Long, List<TeacherSubject>> teacherSubjects;
        private List<InternshipType> internshipTypes;
        private Map<Integer, List<ZoneConstraint>> zoneConstraints;
        private Map<Long, List<InternshipCombinationRule>> combinationRules;
        private Map<String, Subject> fallbackSubjects = new HashMap<>();

        private Map<Long, Integer> currentAssignmentCount = new HashMap<>();
        private Map<Teacher, List<InternshipType>> assignedTypes = new HashMap<>();
        // NEW: Track unique keys (TypeID-SubjectID) to prevent duplicate constraint violation
        private Map<Long, Set<String>> uniqueAssignments = new HashMap<>();

        private Map<Long, Integer> subjectCandidateCount = new HashMap<>();
        private int totalAssignmentsCreated = 0;

        AllocationContext(AllocationParameters params) {
            this.params = params;
        }

        public InternshipType getInternshipType(String code) {
            return internshipTypes.stream().filter(t -> t.getInternshipCode().equals(code)).findFirst().orElse(null);
        }

        public List<InternshipDemand> getDemandsByType(Long typeId) {
            return demands.stream().filter(d -> d.getInternshipType().getId().equals(typeId)).collect(Collectors.toList());
        }

        // Updated to track uniqueness
        public void recordAssignment(Teacher t, InternshipType type, Subject s) {
            currentAssignmentCount.put(t.getId(), currentAssignmentCount.get(t.getId()) + 1);
            assignedTypes.get(t).add(type);
            uniqueAssignments.get(t.getId()).add(type.getId() + "-" + s.getId());
            totalAssignmentsCreated++;
        }

        // NEW: Check if assignment exists
        public boolean hasAssignment(Teacher t, InternshipType type, Subject s) {
            Set<String> teacherAssignments = uniqueAssignments.get(t.getId());
            return teacherAssignments != null && teacherAssignments.contains(type.getId() + "-" + s.getId());
        }

        public int getAssignmentCount(Teacher t) {
            return currentAssignmentCount.getOrDefault(t.getId(), 0);
        }

        public int getTargetAssignments(Teacher t) {
            if (t.getCreditHourBalance() != null && t.getCreditHourBalance() < 0) {
                return params.getMaxAssignmentsPerTeacher();
            }
            return params.getStandardAssignmentsPerTeacher();
        }

        public boolean isTeacherFullyBooked(Teacher t) {
            return getAssignmentCount(t) >= getTargetAssignments(t);
        }

        public void incrementCandidateCount(Long subjectId) {
            subjectCandidateCount.put(subjectId, subjectCandidateCount.getOrDefault(subjectId, 0) + 1);
        }

        public int getCandidateCountForSubject(Long subjectId) {
            return subjectCandidateCount.getOrDefault(subjectId, 0);
        }

        public Subject getFirstQualifiedSubject(Teacher t) {
            List<TeacherSubject> subjects = teacherSubjects.get(t.getId());
            return (subjects != null && !subjects.isEmpty()) ? subjects.get(0).getSubject() : null;
        }

        public Subject getFallbackSubject(String type) {
            return fallbackSubjects.get(type.toUpperCase());
        }
    }

    // --- ACTIVATE (Separate Transaction) ---
    @Transactional
    public void activateAllocationPlan(Long planId) {
        AllocationPlan plan = entityManager.find(AllocationPlan.class, planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found");
        }

        AcademicYear year = plan.getAcademicYear();

        entityManager.createQuery("UPDATE AllocationPlan p SET p.isCurrent = false, p.status = 'ARCHIVED' WHERE p.academicYear.id = :yearId AND p.id != :planId")
                .setParameter("yearId", year.getId()).setParameter("planId", planId).executeUpdate();

        plan.setIsCurrent(true);
        plan.setStatus(AllocationPlan.PlanStatus.APPROVED);
        plan.setUpdatedAt(LocalDateTime.now());
        entityManager.merge(plan);

        entityManager.createQuery("DELETE FROM CreditHourTracking c WHERE c.academicYear.id = :yearId")
                .setParameter("yearId", year.getId()).executeUpdate();
        entityManager.flush();

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByAllocationPlanId(planId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        Map<Teacher, Long> countPerTeacher = assignments.stream().collect(Collectors.groupingBy(TeacherAssignment::getTeacher, Collectors.counting()));

        for (Map.Entry<Teacher, Long> entry : countPerTeacher.entrySet()) {
            Teacher teacher = entry.getKey();
            double credit = (entry.getValue() >= 2) ? 1.0 : 0.0;
            CreditHourTracking tracking = new CreditHourTracking();
            tracking.setTeacher(teacher);
            tracking.setAcademicYear(year);
            tracking.setAssignmentsCount(entry.getValue().intValue());
            tracking.setCreditHoursAllocated(credit);
            tracking.setCreditBalance(0.0);
            tracking.setNotes("Plan " + plan.getPlanVersion() + " Activated");
            tracking.setCreatedAt(LocalDateTime.now());
            entityManager.persist(tracking);
        }
    }
}