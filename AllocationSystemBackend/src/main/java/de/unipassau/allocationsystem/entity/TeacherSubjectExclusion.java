package de.unipassau.allocationsystem.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tracks one-year exceptions where a teacher cannot teach a specific subject
 * they are otherwise qualified for.
 */
@Entity
@Table(name = "teacher_subject_exclusions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"teacher_id", "academic_year_id", "subject_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSubjectExclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "reason")
    private String reason; // e.g., "Teaching load too high in this subject"
}
