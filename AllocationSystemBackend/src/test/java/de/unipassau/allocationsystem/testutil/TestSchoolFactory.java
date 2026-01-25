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
     * Builds a {@link School} with deterministic default values and the provided id/name/type.
     *
     * @param id the school id
     * @param name the school name
     * @param type the school type
     * @return a fully populated {@link School} for tests
     */
    public static School buildTestSchool(Long id, String name, SchoolType type) {
        School school = withDefaultValues();
        // Do not set ID here; let JPA assign the identifier to avoid
        // merge/update behavior and optimistic locking issues in tests.
        if (id != null) {
            school.setId(id);
        }
        school.setSchoolName(name);
        school.setSchoolType(type);
        return school;
    }

    private static School withDefaultValues() {
        School school = new School();
        school.setZoneNumber(1);
        school.setAddress("Test Street 1");
        school.setLatitude(BigDecimal.valueOf(48.5734053));
        school.setLongitude(BigDecimal.valueOf(13.4579944));
        school.setDistanceFromCenter(BigDecimal.valueOf(2.5));
        school.setTransportAccessibility("Bus Line 1");
        school.setContactEmail("test@school.de");
        school.setContactPhone("+49841123456");
        school.setIsActive(true);
        return school;
    }
}
