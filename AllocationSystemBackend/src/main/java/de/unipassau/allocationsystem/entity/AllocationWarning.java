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
 * Entity representing allocation warnings for unmet demands or constraint violations.
 */
@Entity
@Table(name = "allocation_warnings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllocationWarning {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_plan_id", nullable = false)
    private AllocationPlan allocationPlan;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_type_id")
    private InternshipType internshipType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
    
    @Column(name = "school_type", length = 50)
    private String schoolType;
    
    @Min(0)
    @Column(name = "shortage")
    private Integer shortage;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "warning_type", length = 50)
    private WarningType warningType;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public enum WarningType {
        TEACHER_SHORTAGE,
        ZONE_CONSTRAINT_VIOLATION,
        COMBINATION_RULE_VIOLATION,
        SUBJECT_MISMATCH,
        OTHER
    }
}

