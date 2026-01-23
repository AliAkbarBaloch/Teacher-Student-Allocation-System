package de.unipassau.allocationsystem.testutil;

import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;

import java.math.BigDecimal;

/**
 * Shared factory for creating {@link School} instances in tests.
 * Centralizes common setup to avoid code duplication across test classes.
 */
public final class TestSchoolFactory {

    private TestSchoolFactory() {
        // utility class
    }

    /**
     * Creates a fully populated {@link School} suitable for service-layer tests.
     *
     * @param id school id
     * @param name school name
     * @param type school type
     * @return school entity
     */
    public static School buildTestSchool(Long id, String name, SchoolType type) {
        School school = new School();
        school.setId(id);
        school.setSchoolName(name);
        school.setSchoolType(type);
        school.setZoneNumber(1);
        school.setAddress("Test Street 1");
        school.setLatitude(new BigDecimal("48.5734053"));
        school.setLongitude(new BigDecimal("13.4579944"));
        school.setDistanceFromCenter(new BigDecimal("2.5"));
        school.setTransportAccessibility("Bus Line 1");
        school.setContactEmail("test@school.de");
        school.setContactPhone("+49841123456");
        school.setIsActive(true);
        return school;
    }
}
