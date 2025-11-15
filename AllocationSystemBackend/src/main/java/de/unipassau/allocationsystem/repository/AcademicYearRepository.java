package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long>, JpaSpecificationExecutor<AcademicYear> {
    Optional<AcademicYear> findByYearName(String yearName);
}
