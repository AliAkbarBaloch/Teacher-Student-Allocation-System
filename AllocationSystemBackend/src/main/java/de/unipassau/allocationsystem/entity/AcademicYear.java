package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_years")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Year is required")
    @Column(name = "year_name", nullable = false, unique = true)
    private String yearName;

    @NotBlank(message = "Total Credit Hours is required")
    @Column(name = "total_credit_hours", nullable = false)
    private Integer totalCreditHours;

    @NotBlank(message = "Elementary School Hours is required")
    @Column(name = "middle_school_hours", nullable = false)
    private Integer elementarySchoolHours;

    @NotBlank(message = "Middle School Hours is required")
    @Column(name = "middle_school_hours", nullable = false)
    private Integer middleSchoolHours;

    @NotBlank(message = "Budget announcement Date is required")
    @Column(name = "budget_announcement_date", nullable = false)
    private LocalDateTime budgetAnnouncementDate;

    @Column(name = "allocation_deadline")
    private LocalDateTime allocationDeadline;

    @Column(name = "is_locked")
    private Boolean isLocked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
