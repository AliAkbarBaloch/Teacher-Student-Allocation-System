package de.unipassau.allocationsystem.repository;

/**
 * Projection interface for aggregated internship demand data.
 */
public interface InternshipDemandAggregation {
    /**
     * Get the internship type ID.
     * 
     * @return internship type ID
     */
    Long getInternshipTypeId();
    
    /**
     * Get the total required teachers for this internship type.
     * 
     * @return total required teachers
     */
    Integer getTotalRequiredTeachers();
}
