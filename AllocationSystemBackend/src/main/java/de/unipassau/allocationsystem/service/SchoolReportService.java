package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.report.school.SchoolMetricsDto;
import de.unipassau.allocationsystem.dto.report.school.SchoolProfileDto;
import de.unipassau.allocationsystem.dto.report.school.SchoolStatusReportDto;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolReportService {

    private final SchoolRepository schoolRepository;

    @Transactional(readOnly = true)
    public SchoolStatusReportDto generateSchoolStatusReport() {
        // 1. Fetch detailed profiles (includes teacher counts)
        List<SchoolProfileDto> profiles = schoolRepository.findAllSchoolProfiles();

        // 2. Calculate Metrics in-memory
        SchoolMetricsDto metrics = calculateMetrics(profiles);

        return SchoolStatusReportDto.builder()
                .metrics(metrics)
                .profiles(profiles)
                .build();
    }

    private SchoolMetricsDto calculateMetrics(List<SchoolProfileDto> profiles) {
        return SchoolMetricsDto.builder()
                .totalSchools(profiles.size())
                .activeSchools((int) profiles.stream().filter(SchoolProfileDto::isActive).count())
                .inactiveSchools((int) profiles.stream().filter(p -> !p.isActive()).count())

                .schoolsByType(profiles.stream()
                        .collect(Collectors.groupingBy(SchoolProfileDto::getSchoolType, Collectors.counting())))

                .schoolsByZone(profiles.stream()
                        .collect(Collectors.groupingBy(SchoolProfileDto::getZoneNumber, Collectors.counting())))

                .schoolsByAccessibility(profiles.stream()
                        .collect(Collectors.groupingBy(p -> p.getTransportAccessibility() != null ? p.getTransportAccessibility() : "Unknown", Collectors.counting())))
                .build();
    }
}
