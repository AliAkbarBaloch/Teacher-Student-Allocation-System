package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an allocation plan instance for an academic year.
 * Supports versioning, status workflow, and plan history management.
 */
@Entity
@Table(
    name = "allocation_plans",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_allocation_plan_year_version", columnNames = {"year_id", "plan_version"})
    },
    indexes = {
        @Index(name = "idx_allocation_plan_year", columnList = "year_id"),
        @Index(name = "idx_allocation_plan_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllocationPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the academic year for this allocation plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year_id", nullable = false, foreignKey = @ForeignKey(name = "fk_allocation_plan_year"))
    @NotNull(message = "Academic year is required")
    private AcademicYear academicYear;

    /**
     * Name of the allocation plan (e.g., "Initial Draft", "Final Version").
     */
    @NotBlank(message = "Plan name is required")
    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    @Column(name = "plan_name", nullable = false, length = 255)
    private String planName;

    /**
     * Version identifier for this plan (e.g., "v1.0", "v2.0", "draft-001").
     * Must be unique within the same academic year.
     */
    @NotBlank(message = "Plan version is required")
    @Size(max = 100, message = "Plan version must not exceed 100 characters")
    @Column(name = "plan_version", nullable = false, length = 100)
    private String planVersion;

    /**
     * Current status of the allocation plan.
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private PlanStatus status;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
     * Enum representing the possible statuses of an allocation plan.
     */
    public enum PlanStatus {
        /**
         * Plan is in draft state and can be edited freely.
         */
        DRAFT("Draft", "Plan is in draft state"),

        /**
         * Plan is under review by administrators.
         */
        IN_REVIEW("In Review", "Plan is under review"),

        /**
         * Plan has been approved and is mostly locked (only admin can edit).
         */
        APPROVED("Approved", "Plan has been approved"),

        /**
         * Plan has been archived and is no longer actively used.
         */
        ARCHIVED("Archived", "Plan has been archived");

        private final String displayName;
        private final String description;

        PlanStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Check if the plan status allows editing.
         * APPROVED plans are locked but admin can still edit.
         */
        public boolean isEditable() {
            return this == DRAFT || this == IN_REVIEW;
        }

        /**
         * Check if the plan is in a final/locked state.
         */
        public boolean isLocked() {
            return this == APPROVED || this == ARCHIVED;
        }
    }
}
