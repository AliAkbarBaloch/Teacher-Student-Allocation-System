package de.unipassau.allocationsystem.dto.teacher;

import de.unipassau.allocationsystem.entity.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating a new teacher.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCreateDto {

    @NotNull(message = "School ID is required")
    @Positive(message = "School ID must be positive")
    private Long schoolId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$",
            message = "Phone must be a valid phone number")
    private String phone;

    @NotNull(message = "Part-time status is required")
    private Boolean isPartTime;

    private Integer workingHoursPerWeek;

    @NotNull(message = "Employment status is required")
    private Teacher.EmploymentStatus employmentStatus;

    private Teacher.UsageCycle usageCycle;

    private Integer creditHourBalance;
}
