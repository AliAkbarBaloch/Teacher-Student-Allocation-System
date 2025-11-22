package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.CreditHourTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CreditHourTrackingRepository extends JpaRepository<CreditHourTracking, Long>, JpaSpecificationExecutor<CreditHourTracking> {
    Optional<CreditHourTracking> findByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);
    List<CreditHourTracking> findByAcademicYearId(Long yearId);
    boolean existsByTeacherIdAndAcademicYearId(Long teacherId, Long yearId);
}
