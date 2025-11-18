package de.unipassau.allocationsystem.dto.school;

import de.unipassau.allocationsystem.entity.School.SchoolType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for school response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolResponseDto {

    private Long id;
    private String schoolName;
    private SchoolType schoolType;
    private Integer zoneNumber;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal distanceFromCenter;
    private String transportAccessibility;
    private String contactEmail;
    private String contactPhone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
