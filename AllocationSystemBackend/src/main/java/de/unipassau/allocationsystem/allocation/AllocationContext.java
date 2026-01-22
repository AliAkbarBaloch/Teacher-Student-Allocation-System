package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.dto.allocation.AllocationParameters;
import de.unipassau.allocationsystem.entity.InternshipCombinationRule;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.entity.TeacherSubjectExclusion;
import de.unipassau.allocationsystem.entity.ZoneConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Context object holding all data and state needed during the allocation process.
 */
class AllocationContext {
    AllocationParameters params;
    List<Teacher> teachers;
    List<InternshipDemand> demands;

    Map<Long, List<TeacherQualification>> qualifications;
    Map<Long, List<TeacherSubjectExclusion>> exclusions;
    Map<Long, List<TeacherAvailability>> availabilities;
    Map<Long, List<TeacherSubject>> teacherSubjects;
    List<InternshipType> internshipTypes;
    Map<Integer, List<ZoneConstraint>> zoneConstraints;
    Map<Long, List<InternshipCombinationRule>> combinationRules;
    final Map<String, Subject> fallbackSubjects = new HashMap<>();

    final Map<Long, Integer> currentAssignmentCount = new HashMap<>();
    final Map<Teacher, List<InternshipType>> assignedTypes = new HashMap<>();
    final Map<Long, Set<String>> uniqueAssignments = new HashMap<>();

    final Map<Long, Integer> subjectCandidateCount = new HashMap<>();
    int totalAssignmentsCreated = 0;

    AllocationContext(AllocationParameters params) {
        this.params = params;
    }

    /**
     * Gets internship type by code.
     * 
     * @param code The internship code
     * @return The matching internship type or null
     */
    public InternshipType getInternshipType(String code) {
        return internshipTypes.stream()
                .filter(t -> t.getInternshipCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets demands filtered by internship type.
     * 
     * @param typeId The internship type ID
     * @return List of demands for the type
     */
    public List<InternshipDemand> getDemandsByType(Long typeId) {
        return demands.stream()
                .filter(d -> d.getInternshipType().getId().equals(typeId))
                .collect(Collectors.toList());
    }

    /**
     * Records a new assignment for tracking.
     * 
     * @param t The teacher
     * @param type The internship type
     * @param s The subject
     */
    public void recordAssignment(Teacher t, InternshipType type, Subject s) {
        currentAssignmentCount.put(t.getId(), currentAssignmentCount.get(t.getId()) + 1);
        assignedTypes.get(t).add(type);
        uniqueAssignments.get(t.getId()).add(type.getId() + "-" + s.getId());
        totalAssignmentsCreated++;
    }

    /**
     * Checks if a specific assignment already exists.
     * 
     * @param t The teacher
     * @param type The internship type
     * @param s The subject
     * @return true if assignment exists
     */
    public boolean hasAssignment(Teacher t, InternshipType type, Subject s) {
        Set<String> teacherAssignments = uniqueAssignments.get(t.getId());
        return teacherAssignments != null && teacherAssignments.contains(type.getId() + "-" + s.getId());
    }

    /**
     * Gets current assignment count for teacher.
     * 
     * @param t The teacher
     * @return Number of assignments
     */
    public int getAssignmentCount(Teacher t) {
        return currentAssignmentCount.getOrDefault(t.getId(), 0);
    }

    /**
     * Gets target assignment count for teacher based on credit balance.
     * 
     * @param t The teacher
     * @return Target number of assignments
     */
    public int getTargetAssignments(Teacher t) {
        if (t.getCreditHourBalance() != null && t.getCreditHourBalance() < 0) {
            return params.getMaxAssignmentsPerTeacher();
        }
        return params.getStandardAssignmentsPerTeacher();
    }

    /**
     * Checks if teacher has reached their assignment limit.
     * 
     * @param t The teacher
     * @return true if fully booked
     */
    public boolean isTeacherFullyBooked(Teacher t) {
        return getAssignmentCount(t) >= getTargetAssignments(t);
    }

    /**
     * Increments the candidate count for a subject.
     * 
     * @param subjectId The subject ID
     */
    public void incrementCandidateCount(Long subjectId) {
        subjectCandidateCount.put(subjectId, subjectCandidateCount.getOrDefault(subjectId, 0) + 1);
    }

    /**
     * Gets candidate count for a subject.
     * 
     * @param subjectId The subject ID
     * @return Number of candidates
     */
    public int getCandidateCountForSubject(Long subjectId) {
        return subjectCandidateCount.getOrDefault(subjectId, 0);
    }

    /**
     * Gets first qualified subject for teacher.
     * 
     * @param t The teacher
     * @return First qualified subject or null
     */
    public Subject getFirstQualifiedSubject(Teacher t) {
        List<TeacherSubject> subjects = teacherSubjects.get(t.getId());
        return (subjects != null && !subjects.isEmpty()) ? subjects.get(0).getSubject() : null;
    }

    /**
     * Gets fallback subject for internship type.
     * 
     * @param type The internship type code
     * @return Fallback subject or null
     */
    public Subject getFallbackSubject(String type) {
        return fallbackSubjects.get(type.toUpperCase());
    }

    /**
     * Initializes tracking data structures for a teacher.
     * 
     * @param t The teacher to initialize tracking for
     */
    public void initializeTeacherTracking(Teacher t) {
        currentAssignmentCount.put(t.getId(), 0);
        assignedTypes.put(t, new ArrayList<>());
        uniqueAssignments.put(t.getId(), new HashSet<>());
    }
}
