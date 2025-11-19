package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.ZoneConstraint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ZoneConstraint entity providing database access operations.
 */
@Repository
public interface ZoneConstraintRepository extends JpaRepository<ZoneConstraint, Long>,
        JpaSpecificationExecutor<ZoneConstraint> {

    /**
     * Check if a zone constraint already exists for the given zone number and internship type ID.
     *
     * @param zoneNumber        the zone number
     * @param internshipTypeId  the internship type ID
     * @return true if exists, false otherwise
     */
    boolean existsByZoneNumberAndInternshipTypeId(Integer zoneNumber, Long internshipTypeId);

    /**
     * Check if a zone constraint exists excluding a specific ID (for update validation).
     *
     * @param zoneNumber        the zone number
     * @param internshipTypeId  the internship type ID
     * @param id                the ID to exclude
     * @return true if exists, false otherwise
     */
    boolean existsByZoneNumberAndInternshipTypeIdAndIdNot(Integer zoneNumber, Long internshipTypeId, Long id);

    /**
     * Find zone constraint by zone number and internship type ID.
     *
     * @param zoneNumber        the zone number
     * @param internshipTypeId  the internship type ID
     * @return Optional containing the zone constraint if found
     */
    Optional<ZoneConstraint> findByZoneNumberAndInternshipTypeId(Integer zoneNumber, Long internshipTypeId);

    /**
     * Find all zone constraints for a specific zone number.
     *
     * @param zoneNumber the zone number
     * @param pageable   pagination information
     * @return Page of zone constraints
     */
    Page<ZoneConstraint> findByZoneNumber(Integer zoneNumber, Pageable pageable);

    /**
     * Find all zone constraints for a specific internship type.
     *
     * @param internshipTypeId the internship type ID
     * @param pageable         pagination information
     * @return Page of zone constraints
     */
    Page<ZoneConstraint> findByInternshipTypeId(Long internshipTypeId, Pageable pageable);

    /**
     * Find all zone constraints by is_allowed flag.
     *
     * @param isAllowed the allowed flag
     * @param pageable  pagination information
     * @return Page of zone constraints
     */
    Page<ZoneConstraint> findByIsAllowed(Boolean isAllowed, Pageable pageable);

    /**
     * Find all zone constraints for a specific zone and allowed status.
     *
     * @param zoneNumber the zone number
     * @param isAllowed  the allowed flag
     * @param pageable   pagination information
     * @return Page of zone constraints
     */
    Page<ZoneConstraint> findByZoneNumberAndIsAllowed(Integer zoneNumber, Boolean isAllowed, Pageable pageable);

    /**
     * Find all zone constraints for a specific internship type and allowed status.
     *
     * @param internshipTypeId the internship type ID
     * @param isAllowed        the allowed flag
     * @param pageable         pagination information
     * @return Page of zone constraints
     */
    Page<ZoneConstraint> findByInternshipTypeIdAndIsAllowed(Long internshipTypeId, Boolean isAllowed, Pageable pageable);

    /**
     * Find all zone constraints matching zone number, internship type, and allowed status.
     *
     * @param zoneNumber       the zone number
     * @param internshipTypeId the internship type ID
     * @param isAllowed        the allowed flag
     * @param pageable         pagination information
     * @return Page of zone constraints
     */
    Page<ZoneConstraint> findByZoneNumberAndInternshipTypeIdAndIsAllowed(
            Integer zoneNumber, Long internshipTypeId, Boolean isAllowed, Pageable pageable);

    /**
     * Find all allowed internship types for a specific zone.
     *
     * @param zoneNumber the zone number
     * @return List of allowed zone constraints
     */
    @Query("SELECT zc FROM ZoneConstraint zc WHERE zc.zoneNumber = :zoneNumber AND zc.isAllowed = true")
    List<ZoneConstraint> findAllowedInternshipTypesForZone(@Param("zoneNumber") Integer zoneNumber);

    /**
     * Find all zones where a specific internship type is allowed.
     *
     * @param internshipTypeId the internship type ID
     * @return List of zone constraints
     */
    @Query("SELECT zc FROM ZoneConstraint zc WHERE zc.internshipType.id = :internshipTypeId AND zc.isAllowed = true")
    List<ZoneConstraint> findZonesAllowingInternshipType(@Param("internshipTypeId") Long internshipTypeId);

    /**
     * Count constraints by zone number.
     *
     * @param zoneNumber the zone number
     * @return count of constraints
     */
    long countByZoneNumber(Integer zoneNumber);

    /**
     * Count constraints by internship type.
     *
     * @param internshipTypeId the internship type ID
     * @return count of constraints
     */
    long countByInternshipTypeId(Long internshipTypeId);
}
