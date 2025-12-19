package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.TeacherSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long>, JpaSpecificationExecutor<TeacherSubject>  {
    List<TeacherSubject> findByTeacherId(Long teacherId);
    List<TeacherSubject> findByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);

    @Query("SELECT t FROM TeacherSubject t WHERE " +
            "(:teacherId IS NULL OR (t.teacher IS NOT NULL AND t.teacher.id = :teacherId)) AND " +
            "(:yearId IS NULL OR (t.academicYear IS NOT NULL AND t.academicYear.id = :yearId)) AND " +
            "(:subjectId IS NULL OR (t.subject IS NOT NULL AND t.subject.id = :subjectId)) AND " +
            "(:availabilityStatus IS NULL OR t.availabilityStatus = :availabilityStatus) AND " +
            "(:gradeFrom IS NULL OR t.gradeLevelFrom >= :gradeFrom) AND " +
            "(:gradeTo IS NULL OR t.gradeLevelTo <= :gradeTo)")
    Page<TeacherSubject> findByFilters(
            @Param("teacherId") Long teacherId,
            @Param("yearId") Long yearId,
            @Param("subjectId") Long subjectId,
            @Param("availabilityStatus") String availabilityStatus,
            @Param("gradeFrom") Integer gradeFrom,
            @Param("gradeTo") Integer gradeTo,
            Pageable pageable
    );
}
