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
 * Service for ZSP (Additional study-accompanying internship) allocation.
 * Handles medium priority allocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class ZSPAllocationService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;

    /**
     * Allocates teachers to ZSP demands.
     * @param context allocation context with all necessary data
     */
    public void allocate(LegacyAllocationContext context) {
        log.info("Processing {} ZSP demands", context.getDemands().size());

        for (InternshipDemand demand : context.getDemands()) {
            int required = demand.getRequiredTeachers();
            List<Teacher> candidates = findCandidates(context, demand);
            
            int assigned = 0;
            for (Teacher teacher : candidates) {
                if (assigned >= required) {
                    break;
                }
                if (context.getAssignmentsCount().getOrDefault(teacher, 0) < 2) {
                    createAssignment(context, teacher, demand);
                    assigned++;
                }
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
    }
}
