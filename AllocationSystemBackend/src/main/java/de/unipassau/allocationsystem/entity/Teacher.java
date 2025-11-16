package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing a teacher in the allocation system.
 * Teachers are linked to schools and will be assigned to subjects for internship supervision.
 */
@Entity
@Table(name = "teachers", indexes = {
        @Index(name = "idx_teacher_school_id", columnList = "school_id"),
        @Index(name = "idx_teacher_email", columnList = "email"),
        @Index(name = "idx_teacher_employment_status", columnList = "employment_status"),
        @Index(name = "idx_teacher_is_active", columnList = "is_active"),
        @Index(name = "idx_teacher_name", columnList = "last_name, first_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "School is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$", 
             message = "Phone must be a valid phone number")
    @Column(name = "phone", length = 20)
    private String phone;

    @NotNull(message = "Part-time status is required")
    @Column(name = "is_part_time", nullable = false)
    private Boolean isPartTime = false;

    @NotNull(message = "Employment status is required")
    @Column(name = "employment_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Column(name = "usage_cycle", length = 50)
    @Enumerated(EnumType.STRING)
    private UsageCycle usageCycle;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.isPartTime == null) {
            this.isPartTime = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enum representing employment status of a teacher.
     */
    public enum EmploymentStatus {
        FULL_TIME,
        PART_TIME,
        ON_LEAVE,
        CONTRACT,
        PROBATION,
        RETIRED
    }

    /**
     * Enum representing usage cycle/availability periods.
     */
    public enum UsageCycle {
        SEMESTER_1,
        SEMESTER_2,
        FULL_YEAR,
        QUARTERLY
    }
}
