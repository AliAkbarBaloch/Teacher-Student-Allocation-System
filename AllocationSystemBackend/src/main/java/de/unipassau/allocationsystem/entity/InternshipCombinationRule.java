package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "internship_combination_rules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"internship_type_1_id", "internship_type_2_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternshipCombinationRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "internship_type_1_id", referencedColumnName = "id")
    private InternshipType internshipType1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "internship_type_2_id", referencedColumnName = "id")
    private InternshipType internshipType2;

    @Column(name = "is_valid_combination", nullable = false)
    private Boolean isValidCombination;

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
