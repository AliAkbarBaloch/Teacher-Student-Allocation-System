package de.unipassau.allocationsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_hour_tracking", indexes = {
        @Index(name = "idx_credit_hour_teacher", columnList = "teacher_id"),
        @Index(name = "idx_credit_hour_year", columnList = "academic_year_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_credit_hour_teacher_year", columnNames = {"teacher_id", "academic_year_id"})
})
@Getter
@Setter
@NoArgsConstructor
/**
 * Entity representing credit hour tracking for teachers across academic years.
 * Tracks hours used, remaining, and rollover status.
 */
public class CreditHourTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @NotNull
    @Min(0)
    @Column(name = "assignments_count", nullable = false)
    private Integer assignmentsCount = 0;

    @NotNull
    @Min(0)
    @Column(name = "credit_hours_allocated", nullable = false)
    private Double creditHoursAllocated = 0.0;

    @Column(name = "credit_balance", nullable = false)
    private Double creditBalance = 0.0;

    @Column(name = "notes")
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
}
