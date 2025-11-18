package de.unipassau.allocationsystem.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateDto {
    @Size(max = 255)
    private String title;  // Optional

    @Size(max = 1000)
    private String description;  // Optional
}
