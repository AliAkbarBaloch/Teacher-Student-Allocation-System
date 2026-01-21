package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.InternshipDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InternshipDemand entity operations.
 * Manages demand tracking for internship types per academic year.
 */
@Repository
public interface InternshipDemandRepository extends JpaRepository<InternshipDemand, Long>, JpaSpecificationExecutor<InternshipDemand> {
	/**
	 * Aggregate internship demands by academic year.
	 * Groups demands by internship type and sums required teachers.
	 * 
	 * @param yearId the academic year ID
	 * @return list of aggregated demand data
	 */
	@org.springframework.data.jpa.repository.Query("SELECT d.internshipType.id as internshipTypeId, SUM(d.requiredTeachers) as totalRequiredTeachers " +
			"FROM InternshipDemand d WHERE d.academicYear.id = :yearId GROUP BY d.internshipType.id")
	java.util.List<InternshipDemandAggregation> aggregateByYear(@org.springframework.data.repository.query.Param("yearId") Long yearId);

    /**
     * Find all internship demands for an academic year with related entities fetched.
     * 
     * @param yearId the academic year ID
     * @return list of internship demands with eager-loaded relationships
     */
    @org.springframework.data.jpa.repository.Query("SELECT d FROM InternshipDemand d " +
            "JOIN FETCH d.academicYear ay " +
            "JOIN FETCH d.internshipType it " +
            "JOIN FETCH d.subject s " +
            "WHERE ay.id = :yearId")
    List<InternshipDemand> findByAcademicYearId(@org.springframework.data.repository.query.Param("yearId") Long yearId);

}
