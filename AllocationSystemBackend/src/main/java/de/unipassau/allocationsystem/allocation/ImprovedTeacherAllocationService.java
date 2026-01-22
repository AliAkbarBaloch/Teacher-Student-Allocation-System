package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.dto.allocation.AllocationParameters;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Improved teacher allocation service implementing an advanced allocation algorithm.
 * Handles both demand-driven and supply-driven allocation phases with scarcity metrics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImprovedTeacherAllocationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AllocationDataLoader dataLoader;
    private final AllocationService allocationService;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    /**
     * Performs teacher allocation for the specified academic year.
     * 
     * @param academicYearId The ID of the academic year
     * @param params Allocation parameters controlling the process
     * @return The created allocation plan with assignments
     */
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
        allocationService.calculateScarcityMetrics(ctx);
        allocationService.allocateByPriority(plan, ctx, "SFP");
        allocationService.allocateByPriority(plan, ctx, "ZSP");
        allocationService.allocateByPriority(plan, ctx, "PDP1");
        allocationService.allocateByPriority(plan, ctx, "PDP2");

        log.info("Phase 1 Complete. Assignments: {}", ctx.totalAssignmentsCreated);

        // --- PHASE 2: SUPPLY DRIVEN (Fixing the 150/28 issue) ---
        if (params.isForceUtilizationOfSurplus()) {
            handleSurplusTeachers(plan, ctx);
        }

        // --- PHASE 3: VALIDATION & FINALIZATION ---
        allocationService.validateBudget(ctx, year);
        finalizePlan(plan);

        return plan;
    }

    // ============================================================================================
    // PHASE 2: SURPLUS HANDLING (With Duplicate Prevention)
    // ============================================================================================

    private void handleSurplusTeachers(AllocationPlan plan, AllocationContext ctx) {
        List<Teacher> underutilized = getUnderutilizedTeachers(ctx);
        log.info("Phase 2: Processing {} underutilized teachers.", underutilized.size());

        SurplusAllocationTypes types = loadSurplusAllocationTypes(ctx);
        
        for (Teacher teacher : underutilized) {
            processUnderutilizedTeacher(plan, ctx, teacher, types);
        }
    }

    private List<Teacher> getUnderutilizedTeachers(AllocationContext ctx) {
        return ctx.teachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) < ctx.getTargetAssignments(t))
                .collect(Collectors.toList());
    }

    private SurplusAllocationTypes loadSurplusAllocationTypes(AllocationContext ctx) {
        return new SurplusAllocationTypes(
                ctx.getInternshipType("PDP1"),
                ctx.getInternshipType("PDP2"),
                ctx.getInternshipType("ZSP"),
                ctx.getInternshipType("SFP")
        );
    }

    private void processUnderutilizedTeacher(AllocationPlan plan, AllocationContext ctx, 
                                             Teacher teacher, SurplusAllocationTypes types) {
        int zone = teacher.getSchool().getZoneNumber();
        int attempts = 0;
        final int maxAttempts = 10;

        while (ctx.getAssignmentCount(teacher) < ctx.getTargetAssignments(teacher) 
                && attempts < maxAttempts) {
            attempts++;
            
            boolean assigned = attemptZoneBasedAssignment(plan, ctx, teacher, zone, types);
            
            if (!assigned) {
                log.warn("Cannot force assignment for Teacher {} (Zone {}) despite being underutilized. "
                        + "Check Qualification/Zone rules.", teacher.getId(), zone);
                break;
            }
        }
    }

    private boolean attemptZoneBasedAssignment(AllocationPlan plan, AllocationContext ctx, 
                                                Teacher teacher, int zone, SurplusAllocationTypes types) {
        if (zone == 3) {
            return attemptZone3Assignment(plan, ctx, teacher, types);
        }
        return attemptZone12Assignment(plan, ctx, teacher, types);
    }

    private boolean attemptZone3Assignment(AllocationPlan plan, AllocationContext ctx, 
                                           Teacher teacher, SurplusAllocationTypes types) {
        return tryForceAssign(plan, ctx, teacher, types.pdp1) 
                || tryForceAssign(plan, ctx, teacher, types.pdp2);
    }

    private boolean attemptZone12Assignment(AllocationPlan plan, AllocationContext ctx, 
                                            Teacher teacher, SurplusAllocationTypes types) {
        return tryForceAssign(plan, ctx, teacher, types.zsp)
                || tryForceAssign(plan, ctx, teacher, types.sfp)
                || tryForceAssign(plan, ctx, teacher, types.pdp1)
                || tryForceAssign(plan, ctx, teacher, types.pdp2);
    }

    /**
     * Helper class to hold internship types for surplus allocation.
     */
    private static class SurplusAllocationTypes {
        private final InternshipType pdp1;
        private final InternshipType pdp2;
        private final InternshipType zsp;
        private final InternshipType sfp;

        SurplusAllocationTypes(InternshipType pdp1, InternshipType pdp2, 
                               InternshipType zsp, InternshipType sfp) {
            this.pdp1 = pdp1;
            this.pdp2 = pdp2;
            this.zsp = zsp;
            this.sfp = sfp;
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
        Subject subject = allocationService.findBestSubjectForSurplus(ctx, t, type);

        if (subject == null) {
            return false; // No valid unique subject found
        }

        // 3. Create Assignment
        allocationService.createAssignment(plan, ctx, t, type, subject, "Forced Surplus Allocation");
        return true;
    }

    // ============================================================================================
    // PLAN LIFECYCLE MANAGEMENT
    // ============================================================================================

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

    private void finalizePlan(AllocationPlan plan) {
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan.setUpdatedAt(LocalDateTime.now());
        entityManager.merge(plan);
        entityManager.flush();
    }

    private AllocationContext buildContext(Long yearId, AllocationParameters params) {
        AllocationContext ctx = new AllocationContext(params);
        loadDataIntoContext(ctx, yearId);
        loadFallbackSubjects(ctx);
        initializeTeacherTracking(ctx);
        return ctx;
    }

    private void loadDataIntoContext(AllocationContext ctx, Long yearId) {
        ctx.teachers = dataLoader.loadAvailableTeachers(yearId);
        ctx.demands = dataLoader.loadInternshipDemands(yearId);
        ctx.qualifications = dataLoader.loadTeacherQualifications();
        ctx.exclusions = dataLoader.loadTeacherExclusions(yearId);
        ctx.availabilities = dataLoader.loadTeacherAvailabilities(yearId);
        ctx.teacherSubjects = dataLoader.loadTeacherSubjects(yearId);
        ctx.internshipTypes = dataLoader.loadInternshipTypes();
        ctx.zoneConstraints = dataLoader.loadZoneConstraints();
        ctx.combinationRules = dataLoader.loadCombinationRules();
    }

    private void loadFallbackSubjects(AllocationContext ctx) {
        List<Subject> allSubjects = entityManager.createQuery("SELECT s FROM Subject s", Subject.class).getResultList();
        ctx.fallbackSubjects.put("PRIMARY", allSubjects.stream()
                .filter(s -> "Primary".equalsIgnoreCase(s.getSchoolType())).findFirst().orElse(null));
        ctx.fallbackSubjects.put("MIDDLE", allSubjects.stream()
                .filter(s -> "Middle".equalsIgnoreCase(s.getSchoolType())).findFirst().orElse(null));
    }

    private void initializeTeacherTracking(AllocationContext ctx) {
        for (Teacher t : ctx.teachers) {
            ctx.initializeTeacherTracking(t);
        }
    }

    /**
     * Activates an allocation plan in a separate transaction.
     * Archives other plans and creates credit hour tracking.
     * 
     * @param planId The plan ID to activate
     */
    @Transactional
    public void activateAllocationPlan(Long planId) {
        AllocationPlan plan = findAndValidatePlan(planId);
        AcademicYear year = plan.getAcademicYear();

        archiveExistingPlans(year.getId(), planId);
        activatePlan(plan);
        deletePreviousCreditTracking(year.getId());
        createCreditHourTrackingForPlan(plan, year);
    }

    private AllocationPlan findAndValidatePlan(Long planId) {
        AllocationPlan plan = entityManager.find(AllocationPlan.class, planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found");
        }
        return plan;
    }

    private void archiveExistingPlans(Long yearId, Long planId) {
        entityManager.createQuery("UPDATE AllocationPlan p SET p.isCurrent = false, p.status = 'ARCHIVED' "
                + "WHERE p.academicYear.id = :yearId AND p.id != :planId")
                .setParameter("yearId", yearId)
                .setParameter("planId", planId)
                .executeUpdate();
    }

    private void activatePlan(AllocationPlan plan) {
        plan.setIsCurrent(true);
        plan.setStatus(AllocationPlan.PlanStatus.APPROVED);
        plan.setUpdatedAt(LocalDateTime.now());
        entityManager.merge(plan);
    }

    private void deletePreviousCreditTracking(Long yearId) {
        entityManager.createQuery("DELETE FROM CreditHourTracking c WHERE c.academicYear.id = :yearId")
                .setParameter("yearId", yearId)
                .executeUpdate();
        entityManager.flush();
    }

    private void createCreditHourTrackingForPlan(AllocationPlan plan, AcademicYear year) {
        List<TeacherAssignment> assignments = teacherAssignmentRepository
                .findByAllocationPlanId(plan.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent();
        
        Map<Teacher, Long> countPerTeacher = assignments.stream()
                .collect(Collectors.groupingBy(TeacherAssignment::getTeacher, Collectors.counting()));

        for (Map.Entry<Teacher, Long> entry : countPerTeacher.entrySet()) {
            createCreditTracking(entry.getKey(), year, entry.getValue(), plan.getPlanVersion());
        }
    }

    private void createCreditTracking(Teacher teacher, AcademicYear year, Long assignmentCount, String planVersion) {
        double credit = (assignmentCount >= 2) ? 1.0 : 0.0;
        CreditHourTracking tracking = new CreditHourTracking();
        tracking.setTeacher(teacher);
        tracking.setAcademicYear(year);
        tracking.setAssignmentsCount(assignmentCount.intValue());
        tracking.setCreditHoursAllocated(credit);
        tracking.setCreditBalance(0.0);
        tracking.setNotes("Plan " + planVersion + " Activated");
        tracking.setCreatedAt(LocalDateTime.now());
        entityManager.persist(tracking);
    }
}