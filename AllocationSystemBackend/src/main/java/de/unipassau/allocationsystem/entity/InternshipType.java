package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an internship type in the allocation system.
 * Defines the different types of internships that teachers can be assigned to.
 */
@Entity
@Table(name = "internship_types", indexes = {
        @Index(name = "idx_internship_type_code", columnList = "internship_code"),
        @Index(name = "idx_internship_type_priority", columnList = "priority_order"),
        @Index(name = "idx_internship_type_subject_specific", columnList = "is_subject_specific")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternshipType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Internship code is required")
    @Size(min = 2, max = 50, message = "Internship code must be between 2 and 50 characters")
    @Column(name = "internship_code", nullable = false, unique = true, length = 50)
    private String internshipCode;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Size(max = 100, message = "Timing must not exceed 100 characters")
    @Column(name = "timing", length = 100)
    private String timing;

    @Size(max = 50, message = "Period type must not exceed 50 characters")
    @Column(name = "period_type", length = 50)
    private String periodType;

    @Size(max = 50, message = "Semester must not exceed 50 characters")
    @Column(name = "semester", length = 50)
    private String semester;

    @Column(name = "is_subject_specific", nullable = false)
    private Boolean isSubjectSpecific = false;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
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
        this.updatedAt = LocalDateTime.now();
    }
}
