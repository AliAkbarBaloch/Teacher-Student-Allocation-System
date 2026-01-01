package de.unipassau.allocationsystem.dto.subject; 

import lombok.AllArgsConstructor;
import lombok.Getter; 
import lombok.NoArgsConstructor;
import lombok.Setter; 

//minimal subject DTO used inside other responses 

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectSimpleDto
{
    private Long id; 
    private String subjectTitle; 
}