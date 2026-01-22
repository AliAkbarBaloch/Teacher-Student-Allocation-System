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
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Context object for legacy TeacherAllocationService.
 * Encapsulates allocation data to reduce parameter counts.
 */
@Getter
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

    private LegacyAllocationContext(Builder builder) {
        this.allocationPlan = builder.allocationPlan;
        this.teachers = builder.teachers;
        this.demands = builder.demands;
        this.teacherQualifications = builder.teacherQualifications;
        this.teacherExclusions = builder.teacherExclusions;
        this.teacherAvailabilities = builder.teacherAvailabilities;
        this.teacherSubjects = builder.teacherSubjects;
        this.zoneConstraints = builder.zoneConstraints;
        this.assignmentsCount = builder.assignmentsCount;
        this.assignedTypes = builder.assignedTypes;
        this.combinationRules = builder.combinationRules;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AllocationPlan allocationPlan;
        private List<Teacher> teachers;
        private List<InternshipDemand> demands;
        private Map<Long, List<TeacherQualification>> teacherQualifications;
        private Map<Long, List<TeacherSubjectExclusion>> teacherExclusions;
        private Map<Long, List<TeacherAvailability>> teacherAvailabilities;
        private Map<Long, List<TeacherSubject>> teacherSubjects;
        private Map<Integer, List<ZoneConstraint>> zoneConstraints;
        private Map<Teacher, Integer> assignmentsCount;
        private Map<Teacher, List<InternshipType>> assignedTypes;
        private Map<Long, List<InternshipCombinationRule>> combinationRules;

        public Builder allocationPlan(AllocationPlan allocationPlan) {
            this.allocationPlan = allocationPlan;
            return this;
        }

        public Builder teachers(List<Teacher> teachers) {
            this.teachers = teachers;
            return this;
        }

        public Builder demands(List<InternshipDemand> demands) {
            this.demands = demands;
            return this;
        }

        public Builder teacherQualifications(Map<Long, List<TeacherQualification>> teacherQualifications) {
            this.teacherQualifications = teacherQualifications;
            return this;
        }

        public Builder teacherExclusions(Map<Long, List<TeacherSubjectExclusion>> teacherExclusions) {
            this.teacherExclusions = teacherExclusions;
            return this;
        }

        public Builder teacherAvailabilities(Map<Long, List<TeacherAvailability>> teacherAvailabilities) {
            this.teacherAvailabilities = teacherAvailabilities;
            return this;
        }

        public Builder teacherSubjects(Map<Long, List<TeacherSubject>> teacherSubjects) {
            this.teacherSubjects = teacherSubjects;
            return this;
        }

        public Builder zoneConstraints(Map<Integer, List<ZoneConstraint>> zoneConstraints) {
            this.zoneConstraints = zoneConstraints;
            return this;
        }

        public Builder assignmentsCount(Map<Teacher, Integer> assignmentsCount) {
            this.assignmentsCount = assignmentsCount;
            return this;
        }

        public Builder assignedTypes(Map<Teacher, List<InternshipType>> assignedTypes) {
            this.assignedTypes = assignedTypes;
            return this;
        }

        public Builder combinationRules(Map<Long, List<InternshipCombinationRule>> combinationRules) {
            this.combinationRules = combinationRules;
            return this;
        }

        public LegacyAllocationContext build() {
            return new LegacyAllocationContext(this);
        }
    }
}
