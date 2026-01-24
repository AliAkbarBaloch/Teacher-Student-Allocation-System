package de.unipassau.allocationsystem.service.report;

import de.unipassau.allocationsystem.dto.report.teacher.TeacherMetricsDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherProfileDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherStatusReportDto;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Service for generating teacher-related reports.
 * Provides teacher status reports with metrics and profiles.
 */
public class TeacherReportService {
    private final TeacherRepository teacherRepository;
    private final TeacherAvailabilityRepository availabilityRepository;

    /**
     * Generates a comprehensive teacher status report.
     * Includes metrics and individual teacher profiles with qualifications and availability.
     *
     * @param academicYearId optional academic year ID for filtering availability
     * @return teacher status report DTO
     */
    @Transactional(readOnly = true)
    public TeacherStatusReportDto generateTeacherStatusReport(Long academicYearId) {
        List<Teacher> teachers = teacherRepository.findAllWithDetails();

        Map<Long, List<TeacherAvailability>> yearAvailabilityMap = new HashMap<>();
        if (academicYearId != null) {
            List<TeacherAvailability> availabilities = availabilityRepository.findByAcademicYearId(academicYearId);
            yearAvailabilityMap = availabilities.stream()
                    .collect(Collectors.groupingBy(a -> a.getTeacher().getId()));
        }

        TeacherMetricsDto metrics = calculateMetrics(teachers);

        Map<Long, List<TeacherAvailability>> finalYearAvailabilityMap = yearAvailabilityMap;
        List<TeacherProfileDto> profiles = teachers.stream()
                .map(t -> mapToProfile(t, finalYearAvailabilityMap.get(t.getId())))
                .collect(Collectors.toList());

        return TeacherStatusReportDto.builder()
                .metrics(metrics)
                .profiles(profiles)
                .build();
    }

    private TeacherMetricsDto calculateMetrics(List<Teacher> teachers) {
        Map<String, Integer> subjectCounts = new HashMap<>();

        for (Teacher t : teachers) {
            if (t.getQualifications() != null) {
                for (TeacherQualification q : t.getQualifications()) {
                    String subjectName = q.getSubject().getSubjectTitle();
                    subjectCounts.put(subjectName, subjectCounts.getOrDefault(subjectName, 0) + 1);
                }
            }
        }

        return TeacherMetricsDto.builder()
                .totalTeachers(teachers.size())
                .activeCount((int) teachers.stream().filter(t -> t.getEmploymentStatus() == Teacher.EmploymentStatus.ACTIVE).count())
                .onLeaveCount((int) teachers.stream().filter(t -> t.getEmploymentStatus() == Teacher.EmploymentStatus.ON_LEAVE).count())
                .inactiveCount((int) teachers.stream().filter(t -> t.getEmploymentStatus() == Teacher.EmploymentStatus.INACTIVE_THIS_YEAR).count())
                .partTimeCount((int) teachers.stream().filter(Teacher::getIsPartTime).count())
                .fullTimeCount((int) teachers.stream().filter(t -> !t.getIsPartTime()).count())
                .subjectCoverageCounts(subjectCounts)
                .build();
    }

    private TeacherProfileDto mapToProfile(Teacher teacher, List<TeacherAvailability> yearAvailabilities) {
        List<String> subjects = extractQualifiedSubjectCodes(teacher);
        AvailabilitySummary availability = summarizeAvailability(yearAvailabilities);

        return TeacherProfileDto.builder()
                .teacherId(teacher.getId())
                .fullName(buildTeacherFullName(teacher))
                .email(teacher.getEmail())
                .schoolName(getSchoolName(teacher))
                .schoolType(getSchoolType(teacher))
                .employmentStatus(teacher.getEmploymentStatus().name())
                .isPartTime(teacher.getIsPartTime())
                .workingHours(teacher.getWorkingHoursPerWeek())
                .qualifiedSubjects(subjects)
                .availabilityStatusForYear(availability.status)
                .availabilityNotes(availability.notes)
                .build();
    }

    private List<String> extractQualifiedSubjectCodes(Teacher teacher) {
        if (teacher.getQualifications() == null) {
            return List.of();
        }
        return teacher.getQualifications().stream()
                .map(q -> q.getSubject().getSubjectCode())
                .sorted()
                .collect(Collectors.toList());
    }

    private AvailabilitySummary summarizeAvailability(List<TeacherAvailability> yearAvailabilities) {
        if (yearAvailabilities == null || yearAvailabilities.isEmpty()) {
            return new AvailabilitySummary("NOT_SET", "");
        }

        boolean isAvailable = yearAvailabilities.stream()
                .anyMatch(a -> a.getStatus() == TeacherAvailability.AvailabilityStatus.AVAILABLE
                        || a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED);

        String status;
        if (isAvailable) {
            status = "AVAILABLE";
            long preferredCount = yearAvailabilities.stream()
                    .filter(a -> a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED)
                    .count();
            if (preferredCount > 0) {
                status = "PREFERRED";
            }
        } else {
            status = "NOT_AVAILABLE";
        }

        String notes = yearAvailabilities.stream()
                .map(TeacherAvailability::getNotes)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));

        return new AvailabilitySummary(status, notes);
    }

    private String buildTeacherFullName(Teacher teacher) {
        return teacher.getLastName() + ", " + teacher.getFirstName();
    }

    private String getSchoolName(Teacher teacher) {
        if (teacher.getSchool() == null) {
            return "Unknown";
        }
        if (teacher.getSchool().getSchoolName() == null) {
            return "Unknown";
        }
        return teacher.getSchool().getSchoolName();
    }

    private String getSchoolType(Teacher teacher) {
        if (teacher.getSchool() == null || teacher.getSchool().getSchoolType() == null) {
            return "Unknown";
        }
        return teacher.getSchool().getSchoolType().name();
    }

    private static final class AvailabilitySummary {
        private final String status;
        private final String notes;

        private AvailabilitySummary(String status, String notes) {
            this.status = status;
            this.notes = notes;
        }
    }
}
