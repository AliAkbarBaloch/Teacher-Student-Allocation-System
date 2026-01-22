package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling core allocation logic and operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class AllocationService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;

    /**
     * Allocates teachers based on priority for a specific internship type.
     * 
     * @param plan The allocation plan
     * @param ctx The allocation context
     * @param typeCode The internship type code
     */
    public void allocateByPriority(AllocationPlan plan, AllocationContext ctx, String typeCode) {
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

    /**
     * Calculates scarcity metrics for subjects.
     * 
     * @param ctx The allocation context
     */
    public void calculateScarcityMetrics(AllocationContext ctx) {
        for (Teacher t : ctx.teachers) {
            List<TeacherSubject> subs = ctx.teacherSubjects.get(t.getId());
            if (subs != null) {
                for (TeacherSubject ts : subs) {
                    ctx.incrementCandidateCount(ts.getSubject().getId());
                }
            }
        }
    }

    /**
     * Creates and persists a teacher assignment.
     * 
     * @param plan The allocation plan
     * @param ctx The allocation context
     * @param t The teacher
     * @param type The internship type
     * @param s The subject
     * @param note Assignment notes
     */
    public void createAssignment(AllocationPlan plan, AllocationContext ctx, Teacher t, InternshipType type, Subject s, String note) {
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
        ctx.recordAssignment(t, type, s);
    }

    /**
     * Finds the best subject for surplus allocation.
     * 
     * @param ctx The allocation context
     * @param t The teacher
     * @param type The internship type
     * @return The best subject or null
     */
    public Subject findBestSubjectForSurplus(AllocationContext ctx, Teacher t, InternshipType type) {
        List<TeacherQualification> quals = ctx.qualifications.getOrDefault(t.getId(), Collections.emptyList());
        for (TeacherQualification q : quals) {
            Subject s = q.getSubject();
            if (!ctx.hasAssignment(t, type, s)) {
                return s;
            }
        }

        Subject fallback = ctx.getFallbackSubject(t.getSchool().getSchoolType().toString());
        if (fallback != null && !ctx.hasAssignment(t, type, fallback)) {
            return fallback;
        }

        return null;
    }

    /**
     * Validates budget allocation.
     * 
     * @param ctx The allocation context
     * @param year The academic year
     */
    public void validateBudget(AllocationContext ctx, AcademicYear year) {
        long primaryFilled = ctx.teachers.stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY)
                .filter(t -> ctx.getAssignmentCount(t) >= 2).count();
        long middleFilled = ctx.teachers.stream()
                .filter(t -> t.getSchool().getSchoolType() == de.unipassau.allocationsystem.entity.School.SchoolType.MIDDLE)
                .filter(t -> ctx.getAssignmentCount(t) >= 2).count();

        log.info("BUDGET: Primary {}/{}, Middle {}/{}", primaryFilled, year.getElementarySchoolHours(), middleFilled, year.getMiddleSchoolHours());
    }
}
