package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.unipassau.allocationsystem.allocation.AllocationHelper.*;

/**
 * Service for PDP (Pedagogical-didactic block internship) allocation.
 * Handles lowest priority allocation with flexible subject assignment.
 */
@Slf4j
@Service
@RequiredArgsConstructor
class PDPAllocationService {

    private final TeacherAssignmentRepository teacherAssignmentRepository;

    public void allocate(LegacyAllocationContext context) {
        log.info("Processing {} PDP demands", context.getDemands().size());

        for (InternshipDemand demand : context.getDemands()) {
            int required = demand.getRequiredTeachers();
            List<Teacher> candidates = findCandidates(context, demand);
            
            int assigned = 0;
            for (Teacher teacher : candidates) {
                if (assigned >= required) {
                    break;
                }
                if (context.getAssignmentsCount().getOrDefault(teacher, 0) < 2) {
                    Subject assignedSubject = determineSubject(context, teacher, demand);
                    if (assignedSubject != null) {
                        createAssignment(context, teacher, demand, assignedSubject);
                        assigned++;
                    }
                }
            }
        }
    }

    private List<Teacher> findCandidates(LegacyAllocationContext context, InternshipDemand demand) {
        return context.getTeachers().stream()
                .filter(t -> context.getAssignmentsCount().getOrDefault(t, 0) < 2)
                .filter(t -> !isTeacherExcludedFromSubject(t, demand.getSubject(), context.getTeacherExclusions()))
                .filter(t -> isTeacherInAllowedZone(t, demand.getInternshipType(), context.getZoneConstraints()))
                .filter(t -> canTeacherBeAssignedToInternship(t, demand.getInternshipType(), 
                        context.getAssignedTypes(), context.getCombinationRules()))
                .collect(Collectors.toList());
    }

    private Subject determineSubject(LegacyAllocationContext context, Teacher teacher, InternshipDemand demand) {
        if (!demand.getInternshipType().getIsSubjectSpecific()) {
            List<TeacherQualification> qualifications = 
                    context.getTeacherQualifications().getOrDefault(teacher.getId(), new ArrayList<>());
            return qualifications.isEmpty() ? null : qualifications.get(0).getSubject();
        }
        return demand.getSubject();
    }

    private void createAssignment(LegacyAllocationContext context, Teacher teacher, 
                                   InternshipDemand demand, Subject subject) {
        TeacherAssignment assignment = new TeacherAssignment();
        assignment.setAllocationPlan(context.getAllocationPlan());
        assignment.setTeacher(teacher);
        assignment.setInternshipType(demand.getInternshipType());
        assignment.setSubject(subject);
        assignment.setAssignmentStatus(TeacherAssignment.AssignmentStatus.PLANNED);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStudentGroupSize(1);
        teacherAssignmentRepository.save(assignment);

        context.getAssignmentsCount().put(teacher, context.getAssignmentsCount().getOrDefault(teacher, 0) + 1);
        context.getAssignedTypes().computeIfAbsent(teacher, k -> new ArrayList<>()).add(demand.getInternshipType());
    }
}
