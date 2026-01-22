package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.InternshipCombinationRule;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.entity.TeacherSubjectExclusion;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Context object for legacy TeacherAllocationService.
 * Encapsulates allocation data to reduce parameter counts.
 */
@Getter
@Builder
public class LegacyAllocationContext {
    private final AllocationPlan allocationPlan;
    private final List<Teacher> teachers;
    private final List<InternshipDemand> demands;
    private final Map<Long, List<TeacherQualification>> teacherQualifications;
    private final Map<Long, List<TeacherSubjectExclusion>> teacherExclusions;
    private final Map<Long, List<TeacherAvailability>> teacherAvailabilities;
    private final Map<Long, List<TeacherSubject>> teacherSubjects;
    private final Map<Integer, List<ZoneConstraint>> zoneConstraints;
    private final Map<Teacher, Integer> assignmentsCount;
    private final Map<Teacher, List<InternshipType>> assignedTypes;
    private final Map<Long, List<InternshipCombinationRule>> combinationRules;
}
