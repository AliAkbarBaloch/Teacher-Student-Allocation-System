package de.unipassau.allocationsystem.dto.internshiptype.combinationrule;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternshipCombinationRuleCreateDto {
    private Long internshipType1Id;
    private Long internshipType2Id;
    private Boolean isValidCombination;
}
