package de.unipassau.allocationsystem.dto.credittracking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreditHourTrackingResponseDto {
    private Long id;
    private Long teacherId;
    private Long yearId;
    private Integer assignmentsCount;
    private Double creditHoursAllocated;
    private Double creditBalance;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
