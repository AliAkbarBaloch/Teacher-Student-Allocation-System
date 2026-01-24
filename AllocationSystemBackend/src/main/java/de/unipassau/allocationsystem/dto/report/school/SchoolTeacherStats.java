package de.unipassau.allocationsystem.dto.report.school;

/**
 * Simple wrapper holding teacher statistics used by JPQL projections.
 */
public class SchoolTeacherStats {
    private Long totalTeachers;
    private Long activeTeachers;

    public SchoolTeacherStats(Long totalTeachers, Long activeTeachers) {
        this.totalTeachers = totalTeachers == null ? 0L : totalTeachers;
        this.activeTeachers = activeTeachers == null ? 0L : activeTeachers;
    }

    public Long getTotalTeachers() {
        return totalTeachers;
    }

    public Long getActiveTeachers() {
        return activeTeachers;
    }
}
