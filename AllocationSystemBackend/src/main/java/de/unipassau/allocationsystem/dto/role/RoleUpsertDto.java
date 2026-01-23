package de.unipassau.allocationsystem.dto.role;

/**
 * Upsert interface for Role DTO operations.
 * Defines common getter methods for both RoleCreateDto and RoleUpdateDto.
 */
public interface RoleUpsertDto {
    /**
     * Gets the role title.
     * @return role title
     */
    String getTitle();

    /**
     * Gets the role description.
     * @return role description
     */
    String getDescription();
}
