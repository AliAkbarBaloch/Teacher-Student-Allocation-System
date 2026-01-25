package de.unipassau.allocationsystem.dto.internshipdemand;

import lombok.Data;

/**
 * DTO for filtering internship demands with pagination support
 */
@Data
public class InternshipDemandFilterDto {
    private Long yearId;
    private Long internshipTypeId;
    private String schoolType;
    private Long subjectId;
    private Boolean isForecasted;
    private Integer page;
    private Integer size;
    private String sort;
    private String direction;
}
