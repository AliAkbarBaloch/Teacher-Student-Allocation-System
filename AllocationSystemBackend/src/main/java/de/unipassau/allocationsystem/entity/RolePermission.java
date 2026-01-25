package de.unipassau.allocationsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(
   name = "role_permissions",
   uniqueConstraints = {
     @UniqueConstraint(columnNames = {"role_id", "permission_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity representing the association between roles and permissions.
 * Defines which permissions a role has and at what access level.
 */
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @NotNull(message = "Role is required")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    @NotNull(message = "Permission is required")
    private Permission permission;

    @Column(name = "access_level", nullable = false)
    @NotBlank(message = "Access level is required")
    private String accessLevel;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validates that accessLevel contains only allowed values.
     * Called by JPA lifecycle callbacks.
     */
    protected void validateAccessLevel() {
        Set<String> allowedValues = new HashSet<>(Arrays.asList("view", "edit", "update", "delete"));
        Set<String> providedValues = Arrays.stream(accessLevel.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        for (String value : providedValues) {
            if (!allowedValues.contains(value)) {
                throw new IllegalArgumentException(
                        "Invalid access level: " + value + ". Allowed values are: view, edit, update, delete"
                );
            }
        }
    }

    /**
     * Converts the comma-separated access level string into a set of access levels.
     * 
     * @return Set of access level strings (view, edit, update, delete)
     */
    public Set<String> getAccessLevelSet() {
        return Arrays.stream(accessLevel.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    /**
     * Sets the access level from a set of access level strings.
     * Converts the set into a comma-separated string for storage.
     * 
     * @param levels Set of access level strings to convert
     */
    public void setAccessLevelFromSet(Set<String> levels) {
        this.accessLevel = String.join(",", levels);
    }

}
