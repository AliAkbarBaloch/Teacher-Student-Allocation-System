package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.InternshipCombinationRule;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.entity.TeacherSubjectExclusion;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper component for loading allocation-related data from the database.
 * Extracted from TeacherAllocationService to improve maintainability.
 */
@Component
@RequiredArgsConstructor
public class AllocationDataLoader {

    private final EntityManager entityManager;

    /**
     * Loads all available teachers for the academic year.
     */
    public List<Teacher> loadAvailableTeachers(Long academicYearId) {
            return entityManager.createQuery(
                            "SELECT t FROM Teacher t WHERE t.employmentStatus = 'ACTIVE'",
                            Teacher.class)
                            .getResultList();
    }

    /**
     * Loads all internship demands for the academic year.
     */
    public List<InternshipDemand> loadInternshipDemands(Long academicYearId) {
        return entityManager.createQuery(
                        "SELECT d FROM InternshipDemand d WHERE d.academicYear.id = :academicYearId",
                        InternshipDemand.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();
    }

    /**
     * Loads all teacher qualifications grouped by teacher ID.
     */
    public Map<Long, List<TeacherQualification>> loadTeacherQualifications() {
        List<TeacherQualification> qualifications = entityManager.createQuery(
                        "SELECT q FROM TeacherQualification q",
                        TeacherQualification.class)
                .getResultList();

        return qualifications.stream()
                .collect(Collectors.groupingBy(q -> q.getTeacher().getId()));
    }

    /**
     * Loads all teacher exclusions for the academic year grouped by teacher ID.
     */
    public Map<Long, List<TeacherSubjectExclusion>> loadTeacherExclusions(Long academicYearId) {
        List<TeacherSubjectExclusion> exclusions = entityManager.createQuery(
                        "SELECT e FROM TeacherSubjectExclusion e WHERE e.academicYear.id = :academicYearId",
                        TeacherSubjectExclusion.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();

        return exclusions.stream()
                .collect(Collectors.groupingBy(e -> e.getTeacher().getId()));
    }

    /**
     * Loads all teacher availabilities for the academic year grouped by teacher ID.
     */
    public Map<Long, List<TeacherAvailability>> loadTeacherAvailabilities(Long academicYearId) {
        List<TeacherAvailability> availabilities = entityManager.createQuery(
                        "SELECT a FROM TeacherAvailability a WHERE a.academicYear.id = :academicYearId",
                        TeacherAvailability.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();

        return availabilities.stream()
                .collect(Collectors.groupingBy(a -> a.getTeacher().getId()));
    }

    /**
     * Loads all teacher subjects for the academic year grouped by teacher ID.
     */
    public Map<Long, List<TeacherSubject>> loadTeacherSubjects(Long academicYearId) {
        List<TeacherSubject> subjects = entityManager.createQuery(
                        "SELECT ts FROM TeacherSubject ts WHERE ts.academicYear.id = :academicYearId",
                        TeacherSubject.class)
                .setParameter("academicYearId", academicYearId)
                .getResultList();

        return subjects.stream()
                .collect(Collectors.groupingBy(ts -> ts.getTeacher().getId()));
    }

    /**
     * Loads all internship types.
     */
    public List<InternshipType> loadInternshipTypes() {
        return entityManager.createQuery(
                        "SELECT it FROM InternshipType it",
                        InternshipType.class)
                .getResultList();
    }

    /**
     * Loads all zone constraints grouped by zone number.
     */
    public Map<Integer, List<ZoneConstraint>> loadZoneConstraints() {
        List<ZoneConstraint> constraints = entityManager.createQuery(
                        "SELECT z FROM ZoneConstraint z",
                        ZoneConstraint.class)
                .getResultList();

        return constraints.stream()
                .collect(Collectors.groupingBy(ZoneConstraint::getZoneNumber));
    }

    /**
     * Loads all internship combination rules grouped by first internship type ID.
     */
    public Map<Long, List<InternshipCombinationRule>> loadCombinationRules() {
        List<InternshipCombinationRule> rules = entityManager.createQuery(
                        "SELECT r FROM InternshipCombinationRule r",
                        InternshipCombinationRule.class)
                .getResultList();

        return rules.stream()
                .collect(Collectors.groupingBy(r -> r.getInternshipType1().getId()));
    }
}
