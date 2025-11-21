package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teacher_assignment_plan_teacher_internship_subject",
                columnNames = {"plan_id", "teacher_id", "internship_type_id", "subject_id"})
}, indexes = {
        @Index(name = "idx_teacher_assignment_plan_id", columnList = "plan_id"),
        @Index(name = "idx_teacher_assignment_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_teacher_assignment_internship_type_id", columnList = "internship_type_id"),
        @Index(name = "idx_teacher_assignment_subject_id", columnList = "subject_id"),
        @Index(name = "idx_teacher_assignment_status", columnList = "assignment_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_assignment_plan"))
    private AllocationPlan allocationPlan;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_assignment_teacher"))
    private Teacher teacher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_assignment_internship_type"))
    private InternshipType internshipType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_teacher_assignment_subject"))
    private Subject subject;

    @Min(value = 1, message = "Student group size must be at least 1")
    @Column(name = "student_group_size")
    private Integer studentGroupSize = 1;

    @NotNull
    @Column(name = "assignment_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AssignmentStatus assignmentStatus;

    @Column(name = "is_manual_override", nullable = false)
    private Boolean isManualOverride = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.isManualOverride == null) {
            this.isManualOverride = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AssignmentStatus {
        PLANNED,
        CONFIRMED,
        CANCELLED,
        ON_HOLD
    }
}
