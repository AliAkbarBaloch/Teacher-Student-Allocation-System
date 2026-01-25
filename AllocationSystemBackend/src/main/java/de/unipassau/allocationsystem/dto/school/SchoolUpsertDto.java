package de.unipassau.allocationsystem.dto.school;

import de.unipassau.allocationsystem.entity.School.SchoolType;

import java.math.BigDecimal;

/**
 * Upsert interface for School DTO operations.
 * Defines common getter methods for both SchoolCreateDto and SchoolUpdateDto.
 */
public interface SchoolUpsertDto {
    /**
     * Gets the school name.
     * @return school name
     */
    String getSchoolName();

    /**
     * Gets the school type.
     * @return school type
     */
    SchoolType getSchoolType();

    /**
     * Gets the zone number.
     * @return zone number
     */
    Integer getZoneNumber();

    /**
     * Gets the address.
     * @return address
     */
    String getAddress();

    /**
     * Gets the latitude coordinate.
     * @return latitude as BigDecimal
     */
    BigDecimal getLatitude();

    /**
     * Gets the longitude coordinate.
     * @return longitude as BigDecimal
     */
    BigDecimal getLongitude();

    /**
     * Gets the distance from center.
     * @return distance from center as BigDecimal
     */
    BigDecimal getDistanceFromCenter();

    /**
     * Gets the transport accessibility information.
     * @return transport accessibility
     */
    String getTransportAccessibility();

    /**
     * Gets the contact email.
     * @return contact email
     */
    String getContactEmail();

    /**
     * Gets the contact phone.
     * @return contact phone
     */
    String getContactPhone();
}
