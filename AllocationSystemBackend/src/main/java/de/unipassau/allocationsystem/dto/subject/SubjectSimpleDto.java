package de.unipassau.allocationsystem.dto.subject; 

import lombok.AllArgsConstructor;
import lombok.Getter; 
import lombok.NoArgsConstructor;
import lombok.Setter; 

/**
 * Minimal subject DTO used inside other responses.
 *
 * <p>
 * This DTO contains only basic subject information and is intended to be embedded
 * in other DTOs where full subject details are not required.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectSimpleDto {
    private Long id; 
    private String subjectTitle; 
}