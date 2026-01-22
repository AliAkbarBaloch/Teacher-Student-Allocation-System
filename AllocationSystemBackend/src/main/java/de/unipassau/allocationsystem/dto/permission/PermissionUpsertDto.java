package de.unipassau.allocationsystem.dto.permission;

/**
 * Common interface for permission create and update DTOs.
 * Provides shared getters for common fields used in entity mapping.
 */
public interface PermissionUpsertDto {
    /**
     * Gets title.
     * @return Permission title
     */
    String getTitle();

    /**
     * Gets description.
     * @return Permission description
     */
    String getDescription();
}
