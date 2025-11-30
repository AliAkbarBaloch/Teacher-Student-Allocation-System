package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Total Credit Hours is required")
    @Column(name = "total_credit_hours", nullable = false)
    private Integer totalCreditHours;

    @NotNull(message = "Elementary School Hours is required")
    @Column(name = "elementary_school_hours", nullable = false)
    private Integer elementarySchoolHours;

    @NotNull(message = "Middle School Hours is required")
    @Column(name = "middle_school_hours", nullable = false)
    private Integer middleSchoolHours;

    @NotNull(message = "Budget announcement Date is required")
    @Column(name = "budget_announcement_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime budgetAnnouncementDate;

    @Column(name = "allocation_deadline", columnDefinition = "TIMESTAMP")
    private LocalDateTime allocationDeadline;

    @Column(name = "is_locked")
    private Boolean isLocked;

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

    @Override
    public String toString() {
        return "AcademicYear{" +
                "id=" + id +
                ", yearName='" + yearName + '\'' +
                ", totalCreditHours=" + totalCreditHours +
                ", elementarySchoolHours=" + elementarySchoolHours +
                ", middleSchoolHours=" + middleSchoolHours +
                ", budgetAnnouncementDate=" + budgetAnnouncementDate +
                ", allocationDeadline=" + allocationDeadline +
                ", isLocked=" + isLocked +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
