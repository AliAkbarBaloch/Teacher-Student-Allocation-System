package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_qualifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_teacher_qualification", columnNames = {"teacher_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // e.g., A teacher might be qualified for HSU only in Grades 1-2
    @Enumerated(EnumType.STRING)
    @Column(name = "grade_cycle_focus")
    private Teacher.UsageCycle gradeCycleFocus;

    @Column(name = "is_main_subject")
    private Boolean isMainSubject = true; // Helpful for SFP prioritization

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
