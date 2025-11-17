package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.SubjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectCategoryRepository extends JpaRepository<SubjectCategory, Long>, JpaSpecificationExecutor<SubjectCategory> {
    Optional<SubjectCategory> findByCategoryTitle(String categoryTitle);
}

