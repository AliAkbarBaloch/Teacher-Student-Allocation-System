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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
     * Main allocation method - orchestrates the entire allocation process
     *
     * Algorithm Flow:
     * 1. Load and validate data (teachers, demands, constraints)
     * 2. Phase 1: Priority allocations (SFP → ZSP → PDP1 → PDP2)
     * 3. Phase 2: Handle surplus/underutilized teachers
     * 4. Phase 3: Validate and finalize
     */
    @Transactional
    public AllocationPlan performAllocation(Long academicYearId, AllocationParameters params) {
        log.info("=== Starting Teacher Allocation for Academic Year {} ===", academicYearId);

        // PHASE 0: SETUP & VALIDATION
        AcademicYear year = entityManager.find(AcademicYear.class, academicYearId);
        if (year == null) {
            throw new IllegalArgumentException("Academic year not found: " + academicYearId);
        }

        AllocationPlan plan = createDraftPlan(year);
        AllocationContext ctx = buildContext(academicYearId, params);

        log.info("Budget: Total={}, Elementary={}, Middle={}",
                year.getTotalCreditHours(), year.getElementarySchoolHours(), year.getMiddleSchoolHours());
        log.info("Teachers loaded: {}", ctx.getTeachers().size());
        log.info("Demands loaded: {}", ctx.getDemands().size());

        // Separate teachers by school type for budget tracking
        List<Teacher> elementaryTeachers = ctx.getTeachers().stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY)
                .collect(Collectors.toList());
        List<Teacher> middleTeachers = ctx.getTeachers().stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.MIDDLE)
                .collect(Collectors.toList());

        log.info("Elementary teachers: {}, Middle teachers: {}",
                elementaryTeachers.size(), middleTeachers.size());

        int elementaryTarget = year.getElementarySchoolHours() * 2; // Each teacher = 2 assignments
        int middleTarget = year.getMiddleSchoolHours() * 2;
        int totalTarget = elementaryTarget + middleTarget;

        log.info("Targets: Elementary={} assignments, Middle={} assignments, Total={}",
                elementaryTarget, middleTarget, totalTarget);

        // Calculate scarcity metrics for prioritization
        allocationService.calculateScarcityMetrics(ctx);

        // Initialize budget tracking
        BudgetStatus budgetStatus = calculateBudgetStatus(ctx, elementaryTeachers, middleTeachers,
                year.getElementarySchoolHours(), year.getMiddleSchoolHours());

        log.info("Initial Budget: Elementary {}/{}, Middle {}/{}",
                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                budgetStatus.middleUsed, budgetStatus.middleLimit);

        // PHASE 1: PRIORITY ALLOCATIONS (SFP → ZSP → PDP1 → PDP2) WITH BUDGET CONTROL
        log.info("=== PHASE 1: Priority Allocation (Demand-Based with Budget Control) ===");

        // Priority 1: SFP (Summer Wednesday - 100% subject coverage required)
        log.info("--- Allocating SFP (Priority 1) ---");
        allocateByPriorityWithBudget(plan, ctx, "SFP", budgetStatus, elementaryTeachers, middleTeachers);

        logProgressWithBudget(ctx, elementaryTeachers, middleTeachers,
                elementaryTarget, middleTarget, totalTarget);
        log.info("Budget after SFP: Elementary {}/{}, Middle {}/{}",
                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                budgetStatus.middleUsed, budgetStatus.middleLimit);

        // Priority 2: ZSP (Winter Wednesday - subject coverage with flexibility)
        log.info("--- Allocating ZSP (Priority 2) ---");
        allocateByPriorityWithBudget(plan, ctx, "ZSP", budgetStatus, elementaryTeachers, middleTeachers);

        logProgressWithBudget(ctx, elementaryTeachers, middleTeachers,
                elementaryTarget, middleTarget, totalTarget);
        log.info("Budget after ZSP: Elementary {}/{}, Middle {}/{}",
                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                budgetStatus.middleUsed, budgetStatus.middleLimit);

        // Priority 3: PDP1 (Winter Block - no subject requirement)
        log.info("--- Allocating PDP1 (Priority 3a) ---");
        allocateByPriorityWithBudget(plan, ctx, "PDP1", budgetStatus, elementaryTeachers, middleTeachers);

        logProgressWithBudget(ctx, elementaryTeachers, middleTeachers,
                elementaryTarget, middleTarget, totalTarget);
        log.info("Budget after PDP1: Elementary {}/{}, Middle {}/{}",
                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                budgetStatus.middleUsed, budgetStatus.middleLimit);

        // Priority 4: PDP2 (Summer Block - no subject requirement)
        log.info("--- Allocating PDP2 (Priority 3b) ---");
        allocateByPriorityWithBudget(plan, ctx, "PDP2", budgetStatus, elementaryTeachers, middleTeachers);

        logProgressWithBudget(ctx, elementaryTeachers, middleTeachers,
                elementaryTarget, middleTarget, totalTarget);
        log.info("Budget after PDP2: Elementary {}/{}, Middle {}/{}",
                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                budgetStatus.middleUsed, budgetStatus.middleLimit);

        log.info("Phase 1 Complete: {} of {} assignments created",
                ctx.getTotalAssignmentsCreated(), totalTarget);

        // PHASE 2: BUDGET-AWARE AGGRESSIVE SURPLUS HANDLING
        log.info("=== PHASE 2: Budget-Aware Teacher Utilization ===");
        log.info("Starting Phase 2 - Budget Status: Elementary {}/{}, Middle {}/{}",
                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                budgetStatus.middleUsed, budgetStatus.middleLimit);

        int maxPasses = 5;
        int passNumber = 1;

        while (hasUnderutilizedTeachers(ctx) && passNumber <= maxPasses) {
            log.info("--- Pass {}: Filling underutilized teachers (Budget-Aware) ---", passNumber);
            int beforePass = ctx.getTotalAssignmentsCreated();

            log.info("Budget Status before pass: Elementary {}/{}, Middle {}/{}",
                    budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                    budgetStatus.middleUsed, budgetStatus.middleLimit);

            handleSurplusTeachersWithBudgetControl(plan, ctx, passNumber, budgetStatus);

            int assignmentsAdded = ctx.getTotalAssignmentsCreated() - beforePass;
            log.info("Pass {} complete: {} new assignments created", passNumber, assignmentsAdded);
            logProgressWithBudget(ctx, elementaryTeachers, middleTeachers,
                    elementaryTarget, middleTarget, totalTarget);

            if (assignmentsAdded == 0) {
                log.warn("Pass {} made no progress, trying more aggressive approach", passNumber);
            }

            passNumber++;
        }

        // PHASE 3: FINAL FORCED ALLOCATION (if still needed and budget allows)
        if (hasUnderutilizedTeachers(ctx)) {
            log.warn("=== PHASE 3: Force Allocation for Remaining Teachers (Budget-Aware) ===");
            log.info("Budget Status before Phase 3: Elementary {}/{}, Middle {}/{}",
                    budgetStatus.elementaryUsed, budgetStatus.elementaryLimit,
                    budgetStatus.middleUsed, budgetStatus.middleLimit);
            forceAllocateRemainingTeachersWithBudget(plan, ctx, budgetStatus);
        }

        // PHASE 4: VALIDATION & FINALIZATION
        log.info("=== PHASE 4: Final Validation ===");
        validateFinalAllocationWithBudget(ctx, year, elementaryTeachers, middleTeachers, totalTarget);

        finalizePlan(plan);

        log.info("=== Allocation Complete ===");
        log.info("Total assignments: {} of {} ({}%)",
                ctx.getTotalAssignmentsCreated(),
                totalTarget,
                (ctx.getTotalAssignmentsCreated() * 100) / totalTarget);
        log.info("Plan ID: {}, Version: {}", plan.getId(), plan.getPlanVersion());

        return plan;
    }

    /**
     * Budget tracking class
     */
    private static class BudgetStatus {
        int elementaryUsed;      // Credit hours used (teachers with 2+ assignments)
        int elementaryLimit;     // Credit hour budget
        int middleUsed;
        int middleLimit;
        int elementaryRemaining; // How many more teachers can be assigned
        int middleRemaining;

        boolean canAllocateElementary() {
            return elementaryUsed < elementaryLimit;
        }

        boolean canAllocateMiddle() {
            return middleUsed < middleLimit;
        }

        boolean isElementaryOverBudget() {
            return elementaryUsed > elementaryLimit;
        }

        boolean isMiddleOverBudget() {
            return middleUsed > middleLimit;
        }
    }

    /**
     * Calculate current budget usage
     */
    private BudgetStatus calculateBudgetStatus(AllocationContext ctx,
                                               List<Teacher> elementaryTeachers,
                                               List<Teacher> middleTeachers,
                                               int elementaryLimit,
                                               int middleLimit) {
        BudgetStatus status = new BudgetStatus();
        status.elementaryLimit = elementaryLimit;
        status.middleLimit = middleLimit;

        // Count teachers with 2+ assignments (each = 1 credit hour)
        status.elementaryUsed = (int) elementaryTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) >= 2)
                .count();

        status.middleUsed = (int) middleTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) >= 2)
                .count();

        status.elementaryRemaining = elementaryLimit - status.elementaryUsed;
        status.middleRemaining = middleLimit - status.middleUsed;

        return status;
    }

    /**
     * Log progress with budget breakdown
     */
    private void logProgressWithBudget(AllocationContext ctx,
                                       List<Teacher> elementaryTeachers,
                                       List<Teacher> middleTeachers,
                                       int elementaryTarget,
                                       int middleTarget,
                                       int totalTarget) {
        int current = ctx.getTotalAssignmentsCreated();
        int remaining = totalTarget - current;
        int percentage = totalTarget > 0 ? (current * 100) / totalTarget : 0;

        // Elementary stats
        long elemFull = elementaryTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) >= 2)
                .count();
        long elemPartial = elementaryTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) == 1)
                .count();
        long elemNone = elementaryTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) == 0)
                .count();

        // Middle stats
        long midFull = middleTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) >= 2)
                .count();
        long midPartial = middleTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) == 1)
                .count();
        long midNone = middleTeachers.stream()
                .filter(t -> ctx.getAssignmentCount(t) == 0)
                .count();

        log.info("Progress: {}/{} ({}%) | Remaining: {} | " +
                        "Elementary: {} full, {} partial, {} none | " +
                        "Middle: {} full, {} partial, {} none",
                current, totalTarget, percentage, remaining,
                elemFull, elemPartial, elemNone,
                midFull, midPartial, midNone);
    }

    /**
     * Validate final allocation respects budget limits
     */
    private void validateFinalAllocationWithBudget(AllocationContext ctx,
                                                   AcademicYear year,
                                                   List<Teacher> elementaryTeachers,
                                                   List<Teacher> middleTeachers,
                                                   int targetAssignments) {
        BudgetStatus budgetStatus = calculateBudgetStatus(ctx, elementaryTeachers, middleTeachers,
                year.getElementarySchoolHours(), year.getMiddleSchoolHours());

        // Log budget compliance
        log.info("=== Budget Validation ===");
        log.info("Elementary: {}/{} hours used ({})",
                budgetStatus.elementaryUsed,
                budgetStatus.elementaryLimit,
                budgetStatus.isElementaryOverBudget() ? "OVER BUDGET!" : "OK");
        log.info("Middle: {}/{} hours used ({})",
                budgetStatus.middleUsed,
                budgetStatus.middleLimit,
                budgetStatus.isMiddleOverBudget() ? "OVER BUDGET!" : "OK");

        if (budgetStatus.isElementaryOverBudget()) {
            log.error("ERROR: Elementary school budget exceeded by {} hours!",
                    budgetStatus.elementaryUsed - budgetStatus.elementaryLimit);
        }

        if (budgetStatus.isMiddleOverBudget()) {
            log.error("ERROR: Middle school budget exceeded by {} hours!",
                    budgetStatus.middleUsed - budgetStatus.middleLimit);
        }

        // Log underutilized teachers
        List<Teacher> underutilized = ctx.getTeachers().stream()
                .filter(t -> ctx.getAssignmentCount(t) < 2)
                .collect(Collectors.toList());

        if (!underutilized.isEmpty()) {
            log.error("WARNING: {} teachers still underutilized!", underutilized.size());

            long elemUnder = underutilized.stream()
                    .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY)
                    .count();
            long midUnder = underutilized.stream()
                    .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.MIDDLE)
                    .count();

            log.error("  Elementary: {} underutilized", elemUnder);
            log.error("  Middle: {} underutilized", midUnder);

            for (Teacher t : underutilized.stream().limit(10).collect(Collectors.toList())) {
                log.error("  - {} {} ({}, {}) has {} assignments",
                        t.getFirstName(), t.getLastName(),
                        t.getSchool().getSchoolName(),
                        t.getSchool().getSchoolType(),
                        ctx.getAssignmentCount(t));
            }
        }

        int actual = ctx.getTotalAssignmentsCreated();
        if (actual < targetAssignments) {
            log.error("ALLOCATION INCOMPLETE: {}/{} assignments created ({} short)",
                    actual, targetAssignments, targetAssignments - actual);
        } else {
            log.info("ALLOCATION SUCCESS: All {} assignments created", targetAssignments);
        }
    }

    /**
     * PHASE 2: Aggressively handle teachers with < 2 assignments (BUDGET-AWARE)
     *
     * Multiple pass strategy with increasing flexibility:
     * Pass 1: Strict constraints (zone, availability, subject) + Budget check
     * Pass 2: Relaxed availability (any internship type) + Budget check
     * Pass 3: Relaxed subject (use fallback subjects) + Budget check
     * Pass 4: Relaxed zone for some internships + Budget check
     * Pass 5: Force allocation with any valid combination + Budget check
     */
    private void handleSurplusTeachersWithBudgetControl(AllocationPlan plan, AllocationContext ctx,
                                                        int passNumber, BudgetStatus budgetStatus) {
        List<Teacher> underutilized = getUnderutilizedTeachers(ctx);

        if (underutilized.isEmpty()) {
            log.info("All teachers fully utilized (2 assignments each)");
            return;
        }

        log.info("Pass {}: Processing {} underutilized teachers (Budget-aware)",
                passNumber, underutilized.size());

        SurplusAllocationTypes types = loadSurplusAllocationTypes(ctx);

        // Separate by school type
        List<Teacher> elemUnder = underutilized.stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY)
                .collect(Collectors.toList());
        List<Teacher> midUnder = underutilized.stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.MIDDLE)
                .collect(Collectors.toList());

        log.info("  Elementary: {} underutilized (budget: {}/{})",
                elemUnder.size(), budgetStatus.elementaryUsed, budgetStatus.elementaryLimit);
        log.info("  Middle: {} underutilized (budget: {}/{})",
                midUnder.size(), budgetStatus.middleUsed, budgetStatus.middleLimit);

        // Process elementary teachers (if budget allows)
        for (Teacher teacher : elemUnder) {
            int currentAssignments = ctx.getAssignmentCount(teacher);

            while (currentAssignments < ctx.getTargetAssignments(teacher)) {
                // CRITICAL: Check budget BEFORE creating assignment that would complete this teacher
                if (currentAssignments == 1) {
                    // This would be the 2nd assignment - check budget first
                    if (!budgetStatus.canAllocateElementary()) {
                        log.warn("Elementary budget limit reached ({}/{}), cannot complete teacher {} to 2 assignments",
                                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit, teacher.getLastName());
                        break; // Stop this teacher, move to next
                    }
                }

                boolean assigned = attemptSurplusAssignment(plan, ctx, teacher, types, passNumber);

                if (!assigned) {
                    log.debug("Pass {}: Could not assign elementary teacher {} (current: {}/{})",
                            passNumber, teacher.getLastName(),
                            currentAssignments,
                            ctx.getTargetAssignments(teacher));
                    break;
                }

                // Update local counter
                currentAssignments = ctx.getAssignmentCount(teacher);

                // Update budget status if teacher now has 2 assignments (consuming 1 credit hour)
                if (currentAssignments >= 2) {
                    budgetStatus.elementaryUsed++;
                    budgetStatus.elementaryRemaining--;
                    log.debug("Elementary budget updated: {}/{} (completed teacher {})",
                            budgetStatus.elementaryUsed, budgetStatus.elementaryLimit, teacher.getLastName());
                }
            }
        }

        // Process middle school teachers (if budget allows)
        for (Teacher teacher : midUnder) {
            int currentAssignments = ctx.getAssignmentCount(teacher);

            while (currentAssignments < ctx.getTargetAssignments(teacher)) {
                // CRITICAL: Check budget BEFORE creating assignment that would complete this teacher
                if (currentAssignments == 1) {
                    // This would be the 2nd assignment - check budget first
                    if (!budgetStatus.canAllocateMiddle()) {
                        log.warn("Middle school budget limit reached ({}/{}), cannot complete teacher {} to 2 assignments",
                                budgetStatus.middleUsed, budgetStatus.middleLimit, teacher.getLastName());
                        break; // Stop this teacher, move to next
                    }
                }

                boolean assigned = attemptSurplusAssignment(plan, ctx, teacher, types, passNumber);

                if (!assigned) {
                    log.debug("Pass {}: Could not assign middle teacher {} (current: {}/{})",
                            passNumber, teacher.getLastName(),
                            currentAssignments,
                            ctx.getTargetAssignments(teacher));
                    break;
                }

                // Update local counter
                currentAssignments = ctx.getAssignmentCount(teacher);

                // Update budget status if teacher now has 2 assignments (consuming 1 credit hour)
                if (currentAssignments >= 2) {
                    budgetStatus.middleUsed++;
                    budgetStatus.middleRemaining--;
                    log.debug("Middle budget updated: {}/{} (completed teacher {})",
                            budgetStatus.middleUsed, budgetStatus.middleLimit, teacher.getLastName());
                }
            }
        }
    }

    /**
     * Attempt to assign an underutilized teacher based on pass number
     */
    private boolean attemptSurplusAssignment(AllocationPlan plan, AllocationContext ctx,
                                             Teacher teacher, SurplusAllocationTypes types, int passNumber) {
        int zone = teacher.getSchool().getZoneNumber();

        switch (passNumber) {
            case 1:
                // Pass 1: Strict - follow all constraints
                return attemptZoneBasedAssignment(plan, ctx, teacher, zone, types, true, true);

            case 2:
                // Pass 2: Relax availability requirement
                return attemptZoneBasedAssignment(plan, ctx, teacher, zone, types, false, true);

            case 3:
                // Pass 3: Relax subject requirements (use fallback)
                return attemptZoneBasedAssignment(plan, ctx, teacher, zone, types, false, false);

            case 4:
                // Pass 4: Try ANY internship type (ignore zone preference)
                return attemptAnyInternshipType(plan, ctx, teacher, types);

            case 5:
            default:
                // Pass 5: Force allocation with minimal checks
                return forceAssignAnyValid(plan, ctx, teacher, types);
        }
    }

    /**
     * Attempt assignment following zone-based preferences
     */
    private boolean attemptZoneBasedAssignment(AllocationPlan plan, AllocationContext ctx,
                                               Teacher teacher, int zone, SurplusAllocationTypes types,
                                               boolean checkAvailability, boolean requireQualification) {
        if (zone == 3) {
            // Zone 3: Block internships only
            return attemptBlockInternships(plan, ctx, teacher, types, checkAvailability, requireQualification);
        } else {
            // Zone 1 or 2: Wednesday preferred, block as fallback
            return attemptWednesdayFirst(plan, ctx, teacher, types, checkAvailability, requireQualification);
        }
    }

    /**
     * Try block internships (PDP1, PDP2)
     */
    private boolean attemptBlockInternships(AllocationPlan plan, AllocationContext ctx,
                                            Teacher teacher, SurplusAllocationTypes types,
                                            boolean checkAvailability, boolean requireQualification) {
        if (tryAssignWithFlexibility(plan, ctx, teacher, types.pdp1, checkAvailability, requireQualification)) {
            return true;
        }
        return tryAssignWithFlexibility(plan, ctx, teacher, types.pdp2, checkAvailability, requireQualification);
    }

    /**
     * Try Wednesday internships first, then block
     */
    private boolean attemptWednesdayFirst(AllocationPlan plan, AllocationContext ctx,
                                          Teacher teacher, SurplusAllocationTypes types,
                                          boolean checkAvailability, boolean requireQualification) {
        // Try Wednesday internships
        if (tryAssignWithFlexibility(plan, ctx, teacher, types.zsp, checkAvailability, requireQualification)) {
            return true;
        }
        if (tryAssignWithFlexibility(plan, ctx, teacher, types.sfp, checkAvailability, requireQualification)) {
            return true;
        }

        // Fallback to block internships
        if (tryAssignWithFlexibility(plan, ctx, teacher, types.pdp1, checkAvailability, requireQualification)) {
            return true;
        }
        return tryAssignWithFlexibility(plan, ctx, teacher, types.pdp2, checkAvailability, requireQualification);
    }

    /**
     * Try to assign with configurable flexibility
     */
    private boolean tryAssignWithFlexibility(AllocationPlan plan, AllocationContext ctx,
                                             Teacher teacher, InternshipType type,
                                             boolean checkAvailability, boolean requireQualification) {
        if (type == null) {
            return false;
        }

        // Always check zone constraints
        if (!AllocationHelper.isTeacherInAllowedZone(teacher, type, ctx.getZoneConstraints())) {
            return false;
        }

        // Always check combination rules
        if (!AllocationHelper.canTeacherBeAssignedToInternship(teacher, type, ctx.getAssignedTypes(), ctx.getCombinationRules())) {
            return false;
        }

        // Optionally check availability
        if (checkAvailability && !AllocationHelper.isTeacherAvailableForInternship(teacher, type, ctx.getAvailabilities())) {
            return false;
        }

        // Find subject based on qualification requirement
        Subject subject;
        if (requireQualification && type.getIsSubjectSpecific()) {
            subject = allocationService.findBestSubjectForSurplus(ctx, teacher, type);
        } else {
            subject = findAnyValidSubject(ctx, teacher, type);
        }

        if (subject == null) {
            return false;
        }

        // Check for duplicate
        if (ctx.hasAssignment(teacher, type, subject)) {
            return false;
        }

        // Create the assignment
        allocationService.createAssignment(plan, ctx, teacher, type, subject, "Surplus Pass");
        log.debug("Assigned: {} → {} - {} (checkAvail: {}, requireQual: {})",
                teacher.getLastName(), type.getInternshipCode(), subject.getSubjectCode(),
                checkAvailability, requireQualification);

        return true;
    }

    /**
     * Try ANY internship type regardless of zone preference
     */
    private boolean attemptAnyInternshipType(AllocationPlan plan, AllocationContext ctx,
                                             Teacher teacher, SurplusAllocationTypes types) {
        log.debug("Trying ANY internship type for teacher {}", teacher.getLastName());

        // Try all types in order
        InternshipType[] allTypes = {types.pdp1, types.pdp2, types.zsp, types.sfp};

        for (InternshipType type : allTypes) {
            if (tryAssignWithFlexibility(plan, ctx, teacher, type, false, false)) {
                log.info("Successfully assigned {} to {} (relaxed constraints)",
                        teacher.getLastName(), type.getInternshipCode());
                return true;
            }
        }

        return false;
    }

    /**
     * Force assign to any valid combination (last resort)
     */
    private boolean forceAssignAnyValid(AllocationPlan plan, AllocationContext ctx,
                                        Teacher teacher, SurplusAllocationTypes types) {
        log.warn("Force assigning teacher {} - last resort", teacher.getLastName());

        InternshipType[] allTypes = {types.pdp1, types.pdp2, types.zsp, types.sfp};

        for (InternshipType type : allTypes) {
            if (type == null) continue;

            // Only check combination rules (most critical)
            if (!AllocationHelper.canTeacherBeAssignedToInternship(teacher, type,
                    ctx.getAssignedTypes(), ctx.getCombinationRules())) {
                continue;
            }

            // Use any available subject
            Subject subject = findAnyValidSubject(ctx, teacher, type);
            if (subject == null) {
                // Use first available subject from database
                subject = ctx.getFallbackSubject(teacher.getSchool().getSchoolType().toString());
            }

            if (subject == null) {
                continue;
            }

            // Check for duplicate
            if (ctx.hasAssignment(teacher, type, subject)) {
                continue;
            }

            // Force create
            allocationService.createAssignment(plan, ctx, teacher, type, subject, "FORCED - Last Resort");
            log.warn("FORCED assignment: {} → {} - {}",
                    teacher.getLastName(), type.getInternshipCode(), subject.getSubjectCode());
            return true;
        }

        return false;
    }

    /**
     * Find any valid subject for the teacher (with fallback)
     */
    private Subject findAnyValidSubject(AllocationContext ctx, Teacher teacher, InternshipType type) {
        // Try qualified subjects first
        Subject qualified = allocationService.findBestSubjectForSurplus(ctx, teacher, type);
        if (qualified != null) {
            return qualified;
        }

        // Try first qualification
        Subject firstQual = ctx.getFirstQualifiedSubject(teacher);
        if (firstQual != null) {
            return firstQual;
        }

        // Use fallback
        return ctx.getFallbackSubject(teacher.getSchool().getSchoolType().toString());
    }

    /**
     * PHASE 3: Force allocate remaining teachers (BUDGET-AWARE)
     */
    private void forceAllocateRemainingTeachersWithBudget(AllocationPlan plan, AllocationContext ctx,
                                                          BudgetStatus budgetStatus) {
        List<Teacher> remaining = getUnderutilizedTeachers(ctx);

        // Separate by school type
        List<Teacher> elemRemaining = remaining.stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY)
                .collect(Collectors.toList());
        List<Teacher> midRemaining = remaining.stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.MIDDLE)
                .collect(Collectors.toList());

        log.warn("Force allocating: {} elementary, {} middle (Budget-aware)",
                elemRemaining.size(), midRemaining.size());

        SurplusAllocationTypes types = loadSurplusAllocationTypes(ctx);

        // Force allocate elementary (if budget allows)
        for (Teacher teacher : elemRemaining) {
            int currentAssignments = ctx.getAssignmentCount(teacher);

            while (currentAssignments < ctx.getTargetAssignments(teacher)) {
                // CRITICAL: Check budget BEFORE creating assignment that would complete this teacher
                if (currentAssignments == 1) {
                    if (!budgetStatus.canAllocateElementary()) {
                        log.error("Cannot force allocate elementary teacher {} - budget limit reached ({}/{})",
                                teacher.getLastName(), budgetStatus.elementaryUsed, budgetStatus.elementaryLimit);
                        break; // Cannot complete this teacher
                    }
                }

                boolean assigned = forceAssignAnyValid(plan, ctx, teacher, types);

                if (!assigned) {
                    createEmergencyAssignment(plan, ctx, teacher, types);
                }

                // Update counter
                currentAssignments = ctx.getAssignmentCount(teacher);

                // Update budget if teacher completed to 2
                if (currentAssignments >= 2) {
                    budgetStatus.elementaryUsed++;
                    log.debug("Elementary budget: {}/{} after force allocation of {}",
                            budgetStatus.elementaryUsed, budgetStatus.elementaryLimit, teacher.getLastName());
                }
            }
        }

        // Force allocate middle (if budget allows)
        for (Teacher teacher : midRemaining) {
            int currentAssignments = ctx.getAssignmentCount(teacher);

            while (currentAssignments < ctx.getTargetAssignments(teacher)) {
                // CRITICAL: Check budget BEFORE creating assignment that would complete this teacher
                if (currentAssignments == 1) {
                    if (!budgetStatus.canAllocateMiddle()) {
                        log.error("Cannot force allocate middle teacher {} - budget limit reached ({}/{})",
                                teacher.getLastName(), budgetStatus.middleUsed, budgetStatus.middleLimit);
                        break; // Cannot complete this teacher
                    }
                }

                boolean assigned = forceAssignAnyValid(plan, ctx, teacher, types);

                if (!assigned) {
                    createEmergencyAssignment(plan, ctx, teacher, types);
                }

                // Update counter
                currentAssignments = ctx.getAssignmentCount(teacher);

                // Update budget if teacher completed to 2
                if (currentAssignments >= 2) {
                    budgetStatus.middleUsed++;
                    log.debug("Middle budget: {}/{} after force allocation of {}",
                            budgetStatus.middleUsed, budgetStatus.middleLimit, teacher.getLastName());
                }
            }
        }

        // Report budget violations
        if (budgetStatus.isElementaryOverBudget()) {
            log.error("BUDGET VIOLATION: Elementary exceeded by {} hours",
                    budgetStatus.elementaryUsed - budgetStatus.elementaryLimit);
        }
        if (budgetStatus.isMiddleOverBudget()) {
            log.error("BUDGET VIOLATION: Middle exceeded by {} hours",
                    budgetStatus.middleUsed - budgetStatus.middleLimit);
        }
    }

    /**
     * Emergency assignment when all else fails
     */
    private void createEmergencyAssignment(AllocationPlan plan, AllocationContext ctx,
                                           Teacher teacher, SurplusAllocationTypes types) {
        log.error("EMERGENCY: Creating assignment for {} with minimal validation", teacher.getLastName());

        // Try PDP types (most flexible)
        InternshipType type = types.pdp1 != null ? types.pdp1 : types.pdp2;
        if (type == null) {
            type = types.zsp != null ? types.zsp : types.sfp;
        }

        if (type == null) {
            log.error("CRITICAL: No internship types available!");
            return;
        }

        // Get fallback subject
        Subject subject = ctx.getFallbackSubject(teacher.getSchool().getSchoolType().toString());

        if (subject == null) {
            // Get ANY subject from database
            List<Subject> allSubjects = entityManager.createQuery(
                            "SELECT s FROM Subject s WHERE s.isActive = true", Subject.class)
                    .setMaxResults(1)
                    .getResultList();

            if (!allSubjects.isEmpty()) {
                subject = allSubjects.get(0);
            }
        }

        if (subject == null) {
            log.error("CRITICAL: No subjects available for teacher {}", teacher.getLastName());
            return;
        }

        // Create regardless of constraints
        allocationService.createAssignment(plan, ctx, teacher, type, subject,
                "EMERGENCY - MANUAL REVIEW REQUIRED");

        log.error("EMERGENCY assignment created: {} → {} - {} - REQUIRES MANUAL REVIEW",
                teacher.getLastName(), type.getInternshipCode(), subject.getSubjectCode());
    }

    /**
     * Get list of teachers who haven't reached their assignment target
     */
    private List<Teacher> getUnderutilizedTeachers(AllocationContext ctx) {
        return ctx.getTeachers().stream()
                .filter(t -> ctx.getAssignmentCount(t) < ctx.getTargetAssignments(t))
                .sorted((t1, t2) -> Integer.compare(ctx.getAssignmentCount(t1), ctx.getAssignmentCount(t2)))
                .collect(Collectors.toList());
    }

    /**
     * Check if there are any teachers with less than 2 assignments
     */
    private boolean hasUnderutilizedTeachers(AllocationContext ctx) {
        return ctx.getTeachers().stream()
                .anyMatch(t -> ctx.getAssignmentCount(t) < ctx.getTargetAssignments(t));
    }

    /**
     * Allocate by priority WITH budget enforcement
     * This wraps the regular allocation but checks budget before completing teachers to 2 assignments
     */
    private void allocateByPriorityWithBudget(AllocationPlan plan, AllocationContext ctx,
                                              String typeCode, BudgetStatus budgetStatus,
                                              List<Teacher> elementaryTeachers, List<Teacher> middleTeachers) {
        InternshipType type = ctx.getInternshipType(typeCode);
        if (type == null) {
            log.warn("Internship type {} not found", typeCode);
            return;
        }

        List<InternshipDemand> typeDemands = ctx.getDemandsByType(type.getId());

        if (ctx.getParams().isPrioritizeScarcity()) {
            typeDemands.sort(Comparator.comparingInt(d -> ctx.getCandidateCountForSubject(d.getSubject().getId())));
        }

        for (InternshipDemand demand : typeDemands) {
            int required = demand.getRequiredTeachers();
            List<Teacher> candidates = findCandidatesForDemand(ctx, demand);

            // Sort by score
            candidates.sort((t1, t2) -> Integer.compare(
                    scoreTeacherForDemand(t2, demand, ctx),
                    scoreTeacherForDemand(t1, demand, ctx)));

            int assigned = 0;
            for (Teacher teacher : candidates) {
                if (assigned >= required) {
                    break;
                }

                // Check if already assigned to this internship-subject combo
                if (ctx.hasAssignment(teacher, demand.getInternshipType(), demand.getSubject())) {
                    continue;
                }

                // CRITICAL: Check budget before creating assignment
                int currentAssignments = ctx.getAssignmentCount(teacher);
                boolean isElementary = teacher.getSchool().getSchoolType() ==
                        de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY;
                boolean isMiddle = teacher.getSchool().getSchoolType() ==
                        de.unipassau.allocationsystem.entity.School.SchoolType.MIDDLE;

                // If this would complete the teacher to 2 assignments, check budget
                if (currentAssignments == 1) {
                    if (isElementary && !budgetStatus.canAllocateElementary()) {
                        log.warn("Cannot assign {} - elementary budget limit reached ({}/{})",
                                teacher.getLastName(), budgetStatus.elementaryUsed, budgetStatus.elementaryLimit);
                        continue; // Skip this teacher
                    }
                    if (isMiddle && !budgetStatus.canAllocateMiddle()) {
                        log.warn("Cannot assign {} - middle budget limit reached ({}/{})",
                                teacher.getLastName(), budgetStatus.middleUsed, budgetStatus.middleLimit);
                        continue; // Skip this teacher
                    }
                }

                // Create the assignment
                allocationService.createAssignment(plan, ctx, teacher,
                        demand.getInternshipType(), demand.getSubject(), "Demand Match");

                assigned++;

                // Update budget if teacher now has 2 assignments
                int newAssignmentCount = ctx.getAssignmentCount(teacher);
                if (newAssignmentCount >= 2 && currentAssignments < 2) {
                    if (isElementary) {
                        budgetStatus.elementaryUsed++;
                        budgetStatus.elementaryRemaining--;
                        log.debug("Elementary budget: {}/{} after assigning {}",
                                budgetStatus.elementaryUsed, budgetStatus.elementaryLimit, teacher.getLastName());
                    } else if (isMiddle) {
                        budgetStatus.middleUsed++;
                        budgetStatus.middleRemaining--;
                        log.debug("Middle budget: {}/{} after assigning {}",
                                budgetStatus.middleUsed, budgetStatus.middleLimit, teacher.getLastName());
                    }
                }
            }

            if (assigned < required) {
                log.warn("Demand for {} - {} only filled {}/{} (may be due to budget limits)",
                        type.getInternshipCode(), demand.getSubject().getSubjectCode(),
                        assigned, required);
            }
        }
    }

    /**
     * Find candidates for a specific demand
     */
    private List<Teacher> findCandidatesForDemand(AllocationContext ctx, InternshipDemand demand) {
        return ctx.getTeachers().stream()
                .filter(t -> !ctx.isTeacherFullyBooked(t))
                .filter(t -> {
                    if (!demand.getInternshipType().getIsSubjectSpecific()) {
                        return true;
                    }
                    return AllocationHelper.isTeacherQualifiedForSubject(t, demand.getSubject(), ctx.getTeacherSubjects());
                })
                .filter(t -> !AllocationHelper.isTeacherExcludedFromSubject(t, demand.getSubject(), ctx.getExclusions()))
                .filter(t -> AllocationHelper.isTeacherAvailableForInternship(t, demand.getInternshipType(), ctx.getAvailabilities()))
                .filter(t -> AllocationHelper.isTeacherInAllowedZone(t, demand.getInternshipType(), ctx.getZoneConstraints()))
                .filter(t -> AllocationHelper.canTeacherBeAssignedToInternship(t, demand.getInternshipType(), ctx.getAssignedTypes(), ctx.getCombinationRules()))
                .collect(Collectors.toList());
    }

    /**
     * Score a teacher for a specific demand
     */
    private int scoreTeacherForDemand(Teacher t, InternshipDemand d, AllocationContext ctx) {
        int score = 0;

        if (AllocationHelper.hasMainSubjectQualification(t, d.getSubject(), ctx.getQualifications())) {
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

    /**
     * Helper class to hold internship types for surplus allocation
     */
    private static class SurplusAllocationTypes {
        InternshipType pdp1;
        InternshipType pdp2;
        InternshipType zsp;
        InternshipType sfp;
    }

    /**
     * Load internship types for surplus allocation
     */
    private SurplusAllocationTypes loadSurplusAllocationTypes(AllocationContext ctx) {
        SurplusAllocationTypes types = new SurplusAllocationTypes();
        types.pdp1 = ctx.getInternshipType("PDP1");
        types.pdp2 = ctx.getInternshipType("PDP2");
        types.zsp = ctx.getInternshipType("ZSP");
        types.sfp = ctx.getInternshipType("SFP");
        return types;
    }

    // ========== PLAN LIFECYCLE MANAGEMENT ==========

    /**
     * Create a new draft allocation plan
     */
    private AllocationPlan createDraftPlan(AcademicYear year) {
        AllocationPlan plan = new AllocationPlan();
        plan.setAcademicYear(year);
        plan.setPlanName("Allocation Plan " + year.getYearName());
        plan.setPlanVersion("v" + UUID.randomUUID().toString().substring(0, 8));
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan.setIsCurrent(false);
        plan.setNotes("Auto-generated allocation plan");

        entityManager.persist(plan);
        entityManager.flush();

        log.info("Created draft plan: ID={}, Version={}", plan.getId(), plan.getPlanVersion());

        return plan;
    }

    /**
     * Mark plan as ready for review
     */
    private void finalizePlan(AllocationPlan plan) {
        plan.setStatus(AllocationPlan.PlanStatus.IN_REVIEW);
        entityManager.merge(plan);
        entityManager.flush();

        log.info("Plan {} finalized and ready for review", plan.getId());
    }

    /**
     * Build the allocation context with all necessary data
     */
    private AllocationContext buildContext(Long yearId, AllocationParameters params) {
        AllocationContext ctx = new AllocationContext(params);

        loadDataIntoContext(ctx, yearId);
        loadFallbackSubjects(ctx);
        initializeTeacherTracking(ctx);

        return ctx;
    }

    /**
     * Load all data from database into context
     */
    private void loadDataIntoContext(AllocationContext ctx, Long yearId) {
        ctx.setTeachers(dataLoader.loadAvailableTeachers(yearId));
        ctx.setDemands(dataLoader.loadInternshipDemands(yearId));
        ctx.setQualifications(dataLoader.loadTeacherQualifications());
        ctx.setExclusions(dataLoader.loadTeacherExclusions(yearId));
        ctx.setAvailabilities(dataLoader.loadTeacherAvailabilities(yearId));
        ctx.setTeacherSubjects(dataLoader.loadTeacherSubjects(yearId));
        ctx.setInternshipTypes(dataLoader.loadInternshipTypes());
        ctx.setZoneConstraints(dataLoader.loadZoneConstraints());
        ctx.setCombinationRules(dataLoader.loadCombinationRules());
    }

    /**
     * Load fallback subjects for each school type
     * Used when no qualified subject is available
     */
    private void loadFallbackSubjects(AllocationContext ctx) {
        List<Subject> allSubjects = entityManager.createQuery(
                        "SELECT s FROM Subject s WHERE s.isActive = true", Subject.class)
                .getResultList();

        // Find generic subjects for fallback
        for (Subject s : allSubjects) {
            String code = s.getSubjectCode().toUpperCase();

            // Primary fallback: HSU (Sachunterricht)
            if (code.equals("HSU") || s.getSubjectTitle().contains("Sachunterricht")) {
                ctx.getFallbackSubjects().put("PRIMARY", s);
            }

            // Middle fallback: PCB or general science
            if (code.equals("PCB") || code.equals("GSE") || s.getSubjectTitle().contains("Natur")) {
                ctx.getFallbackSubjects().putIfAbsent("MIDDLE", s);
            }
        }

        log.info("Fallback subjects loaded: {}", ctx.getFallbackSubjects().size());
    }

    /**
     * Initialize tracking structures for each teacher
     */
    private void initializeTeacherTracking(AllocationContext ctx) {
        for (Teacher t : ctx.getTeachers()) {
            ctx.initializeTeacherTracking(t);
        }
    }

    // ========== PLAN ACTIVATION ==========

    /**
     * Activate an allocation plan (make it the official current plan)
     *
     * Steps:
     * 1. Validate plan exists and is approved
     * 2. Archive existing active plans
     * 3. Mark new plan as current
     * 4. Create credit hour tracking records
     */
    @Transactional
    public void activateAllocationPlan(Long planId) {
        log.info("Activating allocation plan ID: {}", planId);

        AllocationPlan plan = findAndValidatePlan(planId);
        AcademicYear year = plan.getAcademicYear();

        // Archive existing plans for this year
        archiveExistingPlans(year.getId(), planId);

        // Activate this plan
        activatePlan(plan);

        // Delete previous credit tracking for this year
        deletePreviousCreditTracking(year.getId());

        // Create new credit hour tracking
        createCreditHourTrackingForPlan(plan, year);

        log.info("Plan {} successfully activated for year {}", planId, year.getYearName());
    }

    /**
     * Find and validate the plan can be activated
     */
    private AllocationPlan findAndValidatePlan(Long planId) {
        AllocationPlan plan = entityManager.find(AllocationPlan.class, planId);

        if (plan == null) {
            throw new IllegalArgumentException("Allocation plan not found: " + planId);
        }

        // Plans must be in APPROVED or IN_REVIEW status to be activated
        if (plan.getStatus() != AllocationPlan.PlanStatus.APPROVED
                && plan.getStatus() != AllocationPlan.PlanStatus.IN_REVIEW) {
            throw new IllegalStateException("Only APPROVED or IN_REVIEW plans can be activated. Current status: "
                    + plan.getStatus());
        }

        return plan;
    }

    /**
     * Archive all other plans for this academic year
     */
    private void archiveExistingPlans(Long yearId, Long excludePlanId) {
        List<AllocationPlan> existingPlans = entityManager.createQuery(
                        "SELECT p FROM AllocationPlan p WHERE p.academicYear.id = :yearId AND p.id != :excludeId",
                        AllocationPlan.class)
                .setParameter("yearId", yearId)
                .setParameter("excludeId", excludePlanId)
                .getResultList();

        for (AllocationPlan existingPlan : existingPlans) {
            existingPlan.setIsCurrent(false);
            existingPlan.setStatus(AllocationPlan.PlanStatus.ARCHIVED);
            entityManager.merge(existingPlan);
        }

        log.info("Archived {} existing plans for year {}", existingPlans.size(), yearId);
    }

    /**
     * Mark the plan as current and approved
     */
    private void activatePlan(AllocationPlan plan) {
        plan.setIsCurrent(true);
        plan.setStatus(AllocationPlan.PlanStatus.APPROVED);
        entityManager.merge(plan);
        entityManager.flush();
    }

    /**
     * Delete previous credit hour tracking for this year
     */
    private void deletePreviousCreditTracking(Long yearId) {
        int deleted = entityManager.createQuery(
                        "DELETE FROM CreditHourTracking c WHERE c.academicYear.id = :yearId")
                .setParameter("yearId", yearId)
                .executeUpdate();

        log.info("Deleted {} previous credit hour tracking records", deleted);
    }

    /**
     * Create credit hour tracking records based on plan assignments
     */
    private void createCreditHourTrackingForPlan(AllocationPlan plan, AcademicYear year) {
        // Get all assignments for this plan grouped by teacher
        List<Object[]> teacherAssignmentCounts = entityManager.createQuery(
                        "SELECT ta.teacher, COUNT(ta) FROM TeacherAssignment ta " +
                                "WHERE ta.allocationPlan.id = :planId " +
                                "GROUP BY ta.teacher", Object[].class)
                .setParameter("planId", plan.getId())
                .getResultList();

        for (Object[] row : teacherAssignmentCounts) {
            Teacher teacher = (Teacher) row[0];
            Long assignmentCount = (Long) row[1];

            createCreditTracking(teacher, year, assignmentCount, plan.getPlanVersion());
        }

        log.info("Created {} credit hour tracking records", teacherAssignmentCounts.size());
    }

    /**
     * Create a single credit hour tracking record
     */
    private void createCreditTracking(Teacher teacher, AcademicYear year, Long assignmentCount, String planVersion) {
        CreditHourTracking tracking = new CreditHourTracking();
        tracking.setTeacher(teacher);
        tracking.setAcademicYear(year);
        tracking.setAssignmentsCount(assignmentCount.intValue());

        // Standard: 2 assignments = 1 credit hour
        double creditHours = assignmentCount / 2.0;
        tracking.setCreditHoursAllocated(creditHours);

        // Calculate balance (positive = credit to teacher, negative = debt from teacher)
        int previousBalance = teacher.getCreditHourBalance() != null ? teacher.getCreditHourBalance() : 0;
        double newBalance = previousBalance + (assignmentCount - 2); // Standard is 2 assignments
        tracking.setCreditBalance(newBalance);

        tracking.setNotes("Auto-generated for plan " + planVersion);

        entityManager.persist(tracking);

        log.debug("Credit tracking: Teacher {} - {} assignments, {} credit hours, balance: {}",
                teacher.getLastName(), assignmentCount, creditHours, newBalance);
    }
}