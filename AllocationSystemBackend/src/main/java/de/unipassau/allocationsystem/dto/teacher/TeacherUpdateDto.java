package de.unipassau.allocationsystem.dto.teacher;

import de.unipassau.allocationsystem.entity.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for updating an existing teacher.
 * All fields are optional to allow partial updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherUpdateDto {

    @Positive(message = "School ID must be positive")
    private Long schoolId;

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$",
            message = "Phone must be a valid phone number")
    private String phone;

    private Boolean isPartTime;

    private Teacher.EmploymentStatus employmentStatus;

    private Teacher.UsageCycle usageCycle;

    private Boolean isActive;

    private Integer creditHourBalance;
}
