package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.SubjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SubjectCategory entity operations.
 */
@Repository
public interface SubjectCategoryRepository extends JpaRepository<SubjectCategory, Long>, JpaSpecificationExecutor<SubjectCategory> {
    /**
     * Find subject category by title.
     * 
     * @param categoryTitle the category title
     * @return optional containing the category if found
     */
    Optional<SubjectCategory> findByCategoryTitle(String categoryTitle);
}

