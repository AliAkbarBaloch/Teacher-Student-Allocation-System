package de.unipassau.allocationsystem.dto.report.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolMetricsDto {
    private int totalSchools;
    private int activeSchools;
    private int inactiveSchools;

    // Distribution Maps
    private Map<String, Long> schoolsByType;         // e.g., "PRIMARY": 40
    private Map<Integer, Long> schoolsByZone;        // e.g., Zone 1: 15
    private Map<String, Long> schoolsByAccessibility; // e.g., "4a": 20
}
