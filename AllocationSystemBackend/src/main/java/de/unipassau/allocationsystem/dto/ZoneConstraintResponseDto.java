package de.unipassau.allocationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for zone constraint responses.
 * Contains denormalized data for client consumption.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneConstraintResponseDto {

    private Long id;
    
    private Integer zoneNumber;
    
    private Long internshipTypeId;
    
    private String internshipTypeCode;
    
    private String internshipTypeName;
    
    private Boolean isAllowed;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastModified;
}
