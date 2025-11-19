package de.unipassau.allocationsystem.dto.role;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleUpdateDto {
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title; // Optional for partial update

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description; // Optional
}