package de.unipassau.allocationsystem.dto.school;

import de.unipassau.allocationsystem.entity.School.SchoolType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating an existing school.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolUpdateDto implements SchoolUpsertDto {

    @Size(min = 3, message = "School name must be at least 3 characters")
    private String schoolName;

    private SchoolType schoolType;

    @Positive(message = "Zone number must be positive")
    private Integer zoneNumber;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private BigDecimal distanceFromCenter;

    private String transportAccessibility;

    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$", 
             message = "Contact phone must be a valid phone number")
    private String contactPhone;
}
