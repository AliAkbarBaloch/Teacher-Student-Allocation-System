package de.unipassau.allocationsystem.dto.internshipdemand;

/**
 * DTO for aggregated internship demand data.
 * Summarizes total required teachers per internship type.
 *
 * @param internshipTypeId Internship type identifier
 * @param totalRequiredTeachers Total number of teachers required for this internship type
 */
public record InternshipDemandAggregationDto(Long internshipTypeId, Integer totalRequiredTeachers) {
}
