package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "internship_demands", indexes = {
        @Index(name = "idx_internship_demand_year", columnList = "academic_year_id"),
        @Index(name = "idx_internship_demand_internship_type", columnList = "internship_type_id"),
        @Index(name = "idx_internship_demand_school_type", columnList = "school_type"),
        @Index(name = "idx_internship_demand_subject", columnList = "subject_id")
}, uniqueConstraints = {
//        @UniqueConstraint(name = "uk_internship_demand_unique", columnNames = {"academic_year_id", "internship_type_id", "school_type", "subject_id", "is_forecasted"})
})
@Getter
@Setter
@NoArgsConstructor
public class InternshipDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Academic year is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id", nullable = false)
    private InternshipType internshipType;

    @NotNull
    @Column(name = "school_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private School.SchoolType schoolType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotNull
    @Min(0)
    @Column(name = "required_teachers", nullable = false)
    private Integer requiredTeachers;

    @Min(0)
    @Column(name = "student_count")
    private Integer studentCount;

    @Column(name = "is_forecasted", nullable = false)
    private Boolean isForecasted = Boolean.FALSE;


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
