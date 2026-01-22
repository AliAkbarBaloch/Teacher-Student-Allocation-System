package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.unipassau.allocationsystem.allocation.AllocationHelper.*;

/**
 * Service for SFP (Study-accompanying subject-didactic internship) allocation.
 * Handles the highest priority allocation with strictest rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class SFPAllocationService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;

    /**
     * Allocates teachers for SFP demands.
     *
     * @param context The allocation context containing all necessary data
     */
    public void allocate(LegacyAllocationContext context) {
        log.info("Processing {} SFP demands", context.getDemands().size());

        for (InternshipDemand demand : context.getDemands()) {
            int required = demand.getRequiredTeachers();
            List<Teacher> candidates = findCandidates(context, demand);
            
            candidates = prioritizeCandidates(context, demand, candidates);

            int assigned = 0;
            for (Teacher teacher : candidates) {
                if (assigned >= required) {
                    break;
                }
                if (canAssignTeacher(context, teacher)) {
                    createAssignment(context, teacher, demand);
                    assigned++;
                }
            }

            if (assigned < required) {
                log.warn("Could not fully satisfy SFP demand for subject {} - Assigned {}/{}", 
                        demand.getSubject().getSubjectCode(), assigned, required);
            }
        }
    }

    private List<Teacher> findCandidates(LegacyAllocationContext context, InternshipDemand demand) {
        return context.getTeachers().stream()
                .filter(t -> context.getAssignmentsCount().getOrDefault(t, 0) < 2)
                .filter(t -> isTeacherQualifiedForSubject(t, demand.getSubject(), context.getTeacherSubjects()))
                .filter(t -> !isTeacherExcludedFromSubject(t, demand.getSubject(), context.getTeacherExclusions()))
                .filter(t -> isTeacherInAllowedZone(t, demand.getInternshipType(), context.getZoneConstraints()))
                .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), 
                        context.getAssignedTypes(), context.getCombinationRules()))
                .collect(Collectors.toList());
    }

    private List<Teacher> prioritizeCandidates(LegacyAllocationContext context, InternshipDemand demand, 
                                                List<Teacher> candidates) {
        List<Teacher> prioritized = new ArrayList<>(candidates);
        prioritized.sort((t1, t2) -> {
            boolean t1Main = hasMainSubjectQualification(t1, demand.getSubject(), context.getTeacherQualifications());
            boolean t2Main = hasMainSubjectQualification(t2, demand.getSubject(), context.getTeacherQualifications());
            if (t1Main != t2Main) {
                return t1Main ? -1 : 1;
            }

            int t1Assigned = context.getAssignmentsCount().getOrDefault(t1, 0);
            int t2Assigned = context.getAssignmentsCount().getOrDefault(t2, 0);
            return Integer.compare(t1Assigned, t2Assigned);
        });
        return prioritized;
    }

    private boolean canAssignTeacher(LegacyAllocationContext context, Teacher teacher) {
        return context.getAssignmentsCount().getOrDefault(teacher, 0) < 2;
    }

    private void createAssignment(LegacyAllocationContext context, Teacher teacher, InternshipDemand demand) {
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setAllocationPlan(context.getAllocationPlan());
        assignment.setTeacher(teacher);
        assignment.setInternshipType(demand.getInternshipType());
        assignment.setSubject(demand.getSubject());
        assignment.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStudentGroupSize(1);
        teacherAssignmentRepository.save(assignment);

        context.getAssignmentsCount().put(teacher, context.getAssignmentsCount().getOrDefault(teacher, 0) + 1);
        context.getAssignedTypes().computeIfAbsent(teacher, k -> new ArrayList<>()).add(demand.getInternshipType());
        
        log.debug("Assigned teacher {} to SFP for subject {}", teacher.getId(), demand.getSubject().getSubjectCode());
    }
}
