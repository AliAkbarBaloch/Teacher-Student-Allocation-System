

package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.InternshipCombinationRule;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.entity.TeacherSubjectExclusion;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing helper methods for teacher allocation validation and constraints.
 * Contains methods to check various hard and soft constraints during the allocation process.
 */
public class AllocationHelper {

    /**
     * HARD CONSTRAINT: Checks if teacher is in a zone compatible with the internship type.
     * 
     * @param teacher The teacher to check
     * @param internshipType The internship type to validate against
     * @param zoneConstraints Map of zone constraints by zone number
     * @return true if teacher's zone is allowed for the internship type
     */
    public static boolean isTeacherInAllowedZone(Teacher teacher, InternshipType internshipType, Map<Integer, List<ZoneConstraint>> zoneConstraints) {
        if (teacher == null || teacher.getSchool() == null) {
            return false;
        }
        int zone = teacher.getSchool().getZoneNumber();
        return zoneConstraints.getOrDefault(zone, Collections.emptyList()).stream()
                .anyMatch(c -> c.getInternshipType().getId().equals(internshipType.getId()) && Boolean.TRUE.equals(c.getIsAllowed()));
    }

    /**
     * HARD CONSTRAINT: Checks if teacher can be assigned to internship without type conflicts.
     * Validates that combining this internship type with existing assignments is allowed.
     * 
     * @param teacher The teacher to check
     * @param internshipType The internship type to assign
     * @param assignedTypes Map of currently assigned types per teacher
     * @param combinationRules Map of valid combination rules by internship type ID
     * @return true if the assignment doesn't violate combination rules
     */
    public static boolean canTeacherBeAssignedToInternship(Teacher teacher, InternshipType internshipType, Map<Teacher, List<InternshipType>> assignedTypes, Map<Long, List<InternshipCombinationRule>> combinationRules) {
        List<InternshipType> currentAssignments = assignedTypes.get(teacher);
        if (currentAssignments == null || currentAssignments.isEmpty()) {
            return true;
        }

        for (InternshipType existingType : currentAssignments) {
            List<InternshipCombinationRule> rules = combinationRules.getOrDefault(existingType.getId(), Collections.emptyList());
            boolean isValidCombination = rules.stream()
                    .anyMatch(rule -> rule.getInternshipType2().getId().equals(internshipType.getId()) &&
                            Boolean.TRUE.equals(rule.getIsValidCombination()));
            if (!isValidCombination) {
                return false;
            }
        }
        return true;
    }

    /**
     * SOFT CONSTRAINT: Checks if teacher is qualified for the subject.
     * Used in Demand Phase and for mapping in Surplus phase.
     * 
     * @param teacher The teacher to check
     * @param subject The subject to validate
     * @param teacherSubjects Map of teacher subjects by teacher ID
     * @return true if teacher has AVAILABLE or PREFERRED status for the subject
     */
    public static boolean isTeacherQualifiedForSubject(Teacher teacher, Subject subject, Map<Long, List<TeacherSubject>> teacherSubjects) {
        List<TeacherSubject> subjects = teacherSubjects.getOrDefault(teacher.getId(), Collections.emptyList());
        return subjects.stream()
                .anyMatch(ts -> ts.getSubject().getId().equals(subject.getId()) &&
                        ("AVAILABLE".equalsIgnoreCase(ts.getAvailabilityStatus()) ||
                                "PREFERRED".equalsIgnoreCase(ts.getAvailabilityStatus())));
    }

    /**
     * SOFT CONSTRAINT: Checks if teacher declared availability for the internship type.
     * Note: This constraint is ignored in Surplus Phase.
     * 
     * @param teacher The teacher to check
     * @param internshipType The internship type to validate
     * @param teacherAvailabilities Map of teacher availabilities by teacher ID
     * @return true if teacher has AVAILABLE or PREFERRED status for the internship type
     */
    public static boolean isTeacherAvailableForInternship(Teacher teacher, InternshipType internshipType, Map<Long, List<TeacherAvailability>> teacherAvailabilities) {
        List<TeacherAvailability> availabilities = teacherAvailabilities.getOrDefault(teacher.getId(), Collections.emptyList());
        return availabilities.stream()
                .anyMatch(a -> a.getInternshipType().getId().equals(internshipType.getId()) &&
                        (a.getStatus() == TeacherAvailability.AvailabilityStatus.AVAILABLE ||
                                a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED));
    }

    /**
     * HARD CONSTRAINT: Checks if teacher is explicitly excluded from subject.
     * 
     * @param teacher The teacher to check
     * @param subject The subject to validate
     * @param teacherExclusions Map of teacher exclusions by teacher ID
     * @return true if teacher is excluded from the subject
     */
    public static boolean isTeacherExcludedFromSubject(Teacher teacher, Subject subject, Map<Long, List<TeacherSubjectExclusion>> teacherExclusions) {
        List<TeacherSubjectExclusion> exclusions = teacherExclusions.getOrDefault(teacher.getId(), Collections.emptyList());
        return exclusions.stream().anyMatch(e -> e.getSubject().getId().equals(subject.getId()));
    }

    /**
     * Checks if teacher has main subject qualification for the given subject.
     * 
     * @param teacher The teacher to check
     * @param subject The subject to validate
     * @param teacherQualifications Map of teacher qualifications by teacher ID
     * @return true if teacher has main subject qualification
     */
    public static boolean hasMainSubjectQualification(Teacher teacher, Subject subject, Map<Long, List<TeacherQualification>> teacherQualifications) {
        List<TeacherQualification> qualifications = teacherQualifications.getOrDefault(teacher.getId(), Collections.emptyList());
        return qualifications.stream()
                .anyMatch(q -> q.getSubject().getId().equals(subject.getId()) && Boolean.TRUE.equals(q.getIsMainSubject()));
    }
}
