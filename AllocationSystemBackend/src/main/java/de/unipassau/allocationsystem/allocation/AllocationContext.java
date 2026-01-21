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

import java.util.List;
import java.util.Map;

/**
 * Context object encapsulating all data needed for teacher allocation process.
 * This reduces parameter count and improves code maintainability.
 */
public class AllocationContext {
    
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

    /**
     * Constructs an AllocationContext with all necessary data.
     * 
     * @param allocationPlan The allocation plan being created
     * @param teachers List of all available teachers
     * @param demands List of all internship demands
     * @param teacherQualifications Map of teacher qualifications indexed by teacher ID
     * @param teacherExclusions Map of teacher exclusions indexed by teacher ID
     * @param teacherAvailabilities Map of teacher availabilities indexed by teacher ID
     * @param teacherSubjects Map of teacher subjects indexed by teacher ID
     * @param zoneConstraints Map of zone constraints indexed by zone number
     * @param assignmentsCount Map tracking assignment count per teacher
     * @param assignedTypes Map tracking assigned internship types per teacher
     * @param combinationRules Map of internship combination rules indexed by first type ID
     */
    public AllocationContext(
            AllocationPlan allocationPlan,
            List<Teacher> teachers,
            List<InternshipDemand> demands,
            Map<Long, List<TeacherQualification>> teacherQualifications,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities,
            Map<Long, List<TeacherSubject>> teacherSubjects,
            Map<Integer, List<ZoneConstraint>> zoneConstraints,
            Map<Teacher, Integer> assignmentsCount,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules) {
        this.allocationPlan = allocationPlan;
        this.teachers = teachers;
        this.demands = demands;
        this.teacherQualifications = teacherQualifications;
        this.teacherExclusions = teacherExclusions;
        this.teacherAvailabilities = teacherAvailabilities;
        this.teacherSubjects = teacherSubjects;
        this.zoneConstraints = zoneConstraints;
        this.assignmentsCount = assignmentsCount;
        this.assignedTypes = assignedTypes;
        this.combinationRules = combinationRules;
    }

    public AllocationPlan getAllocationPlan() {
        return allocationPlan;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public List<InternshipDemand> getDemands() {
        return demands;
    }

    public Map<Long, List<TeacherQualification>> getTeacherQualifications() {
        return teacherQualifications;
    }

    public Map<Long, List<TeacherSubjectExclusion>> getTeacherExclusions() {
        return teacherExclusions;
    }

    public Map<Long, List<TeacherAvailability>> getTeacherAvailabilities() {
        return teacherAvailabilities;
    }

    public Map<Long, List<TeacherSubject>> getTeacherSubjects() {
        return teacherSubjects;
    }

    public Map<Integer, List<ZoneConstraint>> getZoneConstraints() {
        return zoneConstraints;
    }

    public Map<Teacher, Integer> getAssignmentsCount() {
        return assignmentsCount;
    }

    public Map<Teacher, List<InternshipType>> getAssignedTypes() {
        return assignedTypes;
    }

    public Map<Long, List<InternshipCombinationRule>> getCombinationRules() {
        return combinationRules;
    }
}
