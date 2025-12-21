package de.unipassau.allocationsystem.service;

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
public class TeacherReportService {
    private final TeacherRepository teacherRepository;
    private final TeacherAvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public TeacherStatusReportDto generateTeacherStatusReport(Long academicYearId) {
        // 1. Fetch all teachers with qualifications loaded
        List<Teacher> teachers = teacherRepository.findAllWithDetails();

        // 2. Fetch Availabilities for the specific year (if provided)
        // Map<TeacherId, List<Availability>>
        Map<Long, List<TeacherAvailability>> yearAvailabilityMap = new HashMap<>();
        if (academicYearId != null) {
            List<TeacherAvailability> availabilities = availabilityRepository.findByAcademicYearId(academicYearId);
            yearAvailabilityMap = availabilities.stream()
                    .collect(Collectors.groupingBy(a -> a.getTeacher().getId()));
        }

        // 3. Calculate Metrics
        TeacherMetricsDto metrics = calculateMetrics(teachers);

        // 4. Build Detail Profiles
        Map<Long, List<TeacherAvailability>> finalYearAvailabilityMap = yearAvailabilityMap; // effective final for lambda
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

        // Count subject qualifications coverage
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
        // Extract Subject Names
        List<String> subjects = teacher.getQualifications().stream()
                .map(q -> q.getSubject().getSubjectCode()) // Or getSubjectTitle()
                .sorted()
                .collect(Collectors.toList());

        // Determine Status for the specific year
        String yearStatus = "NOT_SET";
        String notes = "";

        if (yearAvailabilities != null && !yearAvailabilities.isEmpty()) {
            // A teacher might have multiple availability entries (one per internship type)
            // We summarize: if ANY is available, they are available.
            boolean isAvailable = yearAvailabilities.stream()
                    .anyMatch(a -> a.getStatus() == TeacherAvailability.AvailabilityStatus.AVAILABLE
                            || a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED);

            if (isAvailable) {
                yearStatus = "AVAILABLE";
                // Collect preferences if any
                long preferredCount = yearAvailabilities.stream()
                        .filter(a -> a.getStatus() == TeacherAvailability.AvailabilityStatus.PREFERRED).count();
                if (preferredCount > 0) yearStatus = "PREFERRED";
            } else {
                yearStatus = "NOT_AVAILABLE";
            }

            // Concatenate notes from availability entries
            notes = yearAvailabilities.stream()
                    .map(TeacherAvailability::getNotes)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("; "));
        }

        return TeacherProfileDto.builder()
                .teacherId(teacher.getId())
                .fullName(teacher.getLastName() + ", " + teacher.getFirstName())
                .email(teacher.getEmail())
                .schoolName(teacher.getSchool().getSchoolName())
                .schoolType(teacher.getSchool().getSchoolType().name())
                .employmentStatus(teacher.getEmploymentStatus().name())
                .isPartTime(teacher.getIsPartTime())
                .workingHours(teacher.getWorkingHoursPerWeek())
                .qualifiedSubjects(subjects)
                .availabilityStatusForYear(yearStatus)
                .availabilityNotes(notes)
                .build();
    }
}
