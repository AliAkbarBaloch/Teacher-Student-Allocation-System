package de.unipassau.allocationsystem.dto.zoneconstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for zone constraint response data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneConstraintResponseDto {

    private Long id;
    private Integer zoneNumber;
    private Long internshipTypeId;
    private String internshipTypeCode;
    private String internshipTypeName;
    private Boolean isAllowed;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
