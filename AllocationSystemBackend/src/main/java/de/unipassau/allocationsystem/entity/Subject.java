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
 * Entity representing a subject in the allocation system.
 * Defines subjects that can be assigned to teachers for internships.
 */
@Entity
@Table(name = "subjects", indexes = {
        @Index(name = "idx_subject_code", columnList = "subject_code"),
        @Index(name = "idx_subject_category_id", columnList = "subject_category_id"),
        @Index(name = "idx_subject_school_type", columnList = "school_type"),
        @Index(name = "idx_subject_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject code is required")
    @Size(min = 1, max = 50, message = "Subject code must be between 1 and 50 characters")
    @Column(name = "subject_code", nullable = false, unique = true, length = 50)
    private String subjectCode;

    @NotBlank(message = "Subject title is required")
    @Size(min = 2, max = 255, message = "Subject title must be between 2 and 255 characters")
    @Column(name = "subject_title", nullable = false)
    private String subjectTitle;

    @NotNull(message = "Subject category is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subject_category"))
    private SubjectCategory subjectCategory;

    @Size(max = 50, message = "School type must not exceed 50 characters")
    @Column(name = "school_type", length = 50)
    private String schoolType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
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

