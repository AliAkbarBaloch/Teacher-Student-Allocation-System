package de.unipassau.allocationsystem.dto.internshiptype.combinationrule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for internship combination rule response data.
 * Returns the rule details including which internship types can be combined.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipCombinationRuleResponseDto {
    private Integer id;
    private Long internshipType1Id;
    private Long internshipType2Id;
    private Boolean isValidCombination;
}
