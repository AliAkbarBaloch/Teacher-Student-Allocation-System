package de.unipassau.allocationsystem.dto.zoneconstraint;

/**
 * Upsert interface for ZoneConstraint DTO operations.
 * Defines common getter methods for both ZoneConstraintCreateDto and ZoneConstraintUpdateDto.
 */
public interface ZoneConstraintUpsertDto {
    /**
     * Gets the zone number.
     * @return zone number
     */
    Integer getZoneNumber();

    /**
     * Gets the internship type ID.
     * @return internship type ID
     */
    Long getInternshipTypeId();

    /**
     * Gets whether zone is allowed.
     * @return allowed status
     */
    Boolean getIsAllowed();

    /**
     * Gets the zone description.
     * @return description
     */
    String getDescription();
}
