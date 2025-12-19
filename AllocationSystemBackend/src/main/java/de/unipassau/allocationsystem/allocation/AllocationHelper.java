

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

public class AllocationHelper {

        public static boolean isTeacherInAllowedZone(
                        Teacher teacher,
                        InternshipType internshipType,
                        Map<Integer, List<ZoneConstraint>> zoneConstraints
        ) {
                int teacherZone = teacher.getSchool().getZoneNumber();
                List<ZoneConstraint> constraints = zoneConstraints.getOrDefault(teacherZone, Collections.emptyList());
                return constraints.stream()
                                .anyMatch(c -> c.getInternshipType().getId().equals(internshipType.getId()) && 
                                                          Boolean.TRUE.equals(c.getIsAllowed()));
        }
    public static boolean isTeacherQualifiedForSubject(
            Teacher teacher,
            Subject subject,
            Map<Long, List<TeacherSubject>> teacherSubjects
    ) {
        List<TeacherSubject> subjects = teacherSubjects.getOrDefault(teacher.getId(), Collections.emptyList());
        return subjects.stream()
                .anyMatch(ts -> ts.getSubject().getId().equals(subject.getId()) &&
                              ("AVAILABLE".equalsIgnoreCase(ts.getAvailabilityStatus()) || 
                               "PREFERRED".equalsIgnoreCase(ts.getAvailabilityStatus())));
    }

    public static boolean isTeacherExcludedFromSubject(
            Teacher teacher,
            Subject subject,
            Map<Long, List<TeacherSubjectExclusion>> teacherExclusions
    ) {
        List<TeacherSubjectExclusion> exclusions = teacherExclusions.getOrDefault(teacher.getId(), Collections.emptyList());
        return exclusions.stream()
                .anyMatch(e -> e.getSubject().getId().equals(subject.getId()));
    }

    public static boolean isTeacherAvailableForInternship(
            Teacher teacher,
            InternshipType internshipType,
            Map<Long, List<TeacherAvailability>> teacherAvailabilities
    ) {
        List<TeacherAvailability> availabilities = teacherAvailabilities.getOrDefault(teacher.getId(), Collections.emptyList());
        return availabilities.stream()
                .anyMatch(a -> a.getInternshipType().getId().equals(internshipType.getId()) &&
                              (a.getStatus() == TeacherAvailability.AvailabilityStatus.AVAILABLE ||
                               a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED));
    }

    public static boolean canTeacherBeAssignedToInternship(
            Teacher teacher,
            InternshipType internshipType,
            Map<Teacher, List<InternshipType>> assignedTypes,
            Map<Long, List<InternshipCombinationRule>> combinationRules
    ) {
        List<InternshipType> currentAssignments = assignedTypes.get(teacher);
        if (currentAssignments.isEmpty()) {
            return true;
        }
        
        // Check each existing assignment against the new type
        for (InternshipType existingType : currentAssignments) {
            // Look for a combination rule: existingType -> newType
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

    public static boolean hasMainSubjectQualification(
            Teacher teacher,
            Subject subject,
            Map<Long, List<TeacherQualification>> teacherQualifications
    ) {
        List<TeacherQualification> qualifications = teacherQualifications.getOrDefault(teacher.getId(), Collections.emptyList());
        return qualifications.stream()
                .anyMatch(q -> q.getSubject().getId().equals(subject.getId()) &&
                             Boolean.TRUE.equals(q.getIsMainSubject()));
    }
}
