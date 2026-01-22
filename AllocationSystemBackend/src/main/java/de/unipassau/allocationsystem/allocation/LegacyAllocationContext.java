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

    /**
     * Creates a new builder for LegacyAllocationContext.
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for LegacyAllocationContext.
     */
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

        /**
         * Sets the allocation plan.
         * @param allocationPlan the allocation plan
         * @return this builder
         */
        public Builder allocationPlan(AllocationPlan allocationPlan) {
            this.allocationPlan = allocationPlan;
            return this;
        }

        /**
         * Sets the teachers list.
         * @param teachers list of teachers
         * @return this builder
         */
        public Builder teachers(List<Teacher> teachers) {
            this.teachers = teachers;
            return this;
        }

        /**
         * Sets the internship demands list.
         * @param demands list of internship demands
         * @return this builder
         */
        public Builder demands(List<InternshipDemand> demands) {
            this.demands = demands;
            return this;
        }

        /**
         * Sets the teacher qualifications map.
         * @param teacherQualifications map of teacher qualifications by teacher ID
         * @return this builder
         */
        public Builder teacherQualifications(Map<Long, List<TeacherQualification>> teacherQualifications) {
            this.teacherQualifications = teacherQualifications;
            return this;
        }

        /**
         * Sets the teacher subject exclusions map.
         * @param teacherExclusions map of teacher exclusions by teacher ID
         * @return this builder
         */
        public Builder teacherExclusions(Map<Long, List<TeacherSubjectExclusion>> teacherExclusions) {
            this.teacherExclusions = teacherExclusions;
            return this;
        }

        /**
         * Sets the teacher availabilities map.
         * @param teacherAvailabilities map of teacher availabilities by teacher ID
         * @return this builder
         */
        public Builder teacherAvailabilities(Map<Long, List<TeacherAvailability>> teacherAvailabilities) {
            this.teacherAvailabilities = teacherAvailabilities;
            return this;
        }

        /**
         * Sets the teacher subjects map.
         * @param teacherSubjects map of teacher subjects by teacher ID
         * @return this builder
         */
        public Builder teacherSubjects(Map<Long, List<TeacherSubject>> teacherSubjects) {
            this.teacherSubjects = teacherSubjects;
            return this;
        }

        /**
         * Sets the zone constraints map.
         * @param zoneConstraints map of zone constraints by zone ID
         * @return this builder
         */
        public Builder zoneConstraints(Map<Integer, List<ZoneConstraint>> zoneConstraints) {
            this.zoneConstraints = zoneConstraints;
            return this;
        }

        /**
         * Sets the assignments count map.
         * @param assignmentsCount map of assignment counts by teacher
         * @return this builder
         */
        public Builder assignmentsCount(Map<Teacher, Integer> assignmentsCount) {
            this.assignmentsCount = assignmentsCount;
            return this;
        }

        /**
         * Sets the assigned types map.
         * @param assignedTypes map of assigned internship types by teacher
         * @return this builder
         */
        public Builder assignedTypes(Map<Teacher, List<InternshipType>> assignedTypes) {
            this.assignedTypes = assignedTypes;
            return this;
        }

        /**
         * Sets the combination rules map.
         * @param combinationRules map of combination rules by rule ID
         * @return this builder
         */
        public Builder combinationRules(Map<Long, List<InternshipCombinationRule>> combinationRules) {
            this.combinationRules = combinationRules;
            return this;
        }

        /**
         * Builds the LegacyAllocationContext instance.
         * @return new LegacyAllocationContext instance
         */
        public LegacyAllocationContext build() {
            return new LegacyAllocationContext(this);
        }
    }
}
