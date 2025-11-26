package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing zone constraints for internship types.
 * Defines whether a specific internship type is allowed in a particular zone.
 */
@Entity
@Table(name = "zone_constraints",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_zone_internship_type",
                        columnNames = {"zone_number", "internship_type_id"}
                )
        },
        indexes = {
                @Index(name = "idx_zone_constraints_zone_number", columnList = "zone_number"),
                @Index(name = "idx_zone_constraints_internship_type_id", columnList = "internship_type_id"),
                @Index(name = "idx_zone_constraints_is_allowed", columnList = "is_allowed")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZoneConstraint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Zone number is required")
    @Min(value = 1, message = "Zone number must be positive")
    @Column(name = "zone_number", nullable = false)
    private Integer zoneNumber;

    @NotNull(message = "Internship type is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_zone_constraint_internship_type"))
    private InternshipType internshipType;

    @NotNull(message = "Is allowed flag is required")
    @Column(name = "is_allowed", nullable = false)
    private Boolean isAllowed = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get a display-friendly description of this zone constraint
     */
    public String getDisplayName() {
        String allowedStr = isAllowed ? "allowed" : "not allowed";
        String typeName = internshipType != null ? internshipType.getInternshipCode() : "Unknown";
        return String.format("Zone %d: %s is %s", zoneNumber, typeName, allowedStr);
    }

    /**
     * Check if this constraint allows the internship type
     */
    public boolean isInternshipAllowed() {
        return Boolean.TRUE.equals(isAllowed);
    }
}
