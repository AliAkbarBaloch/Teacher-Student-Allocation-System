package de.unipassau.allocationsystem.mapper.util;

import java.util.function.Consumer;

/**
 * Utility for common mapper operations.
 * Provides reusable helper methods for entity updates and DTO operations.
 */
public final class MapperUtil {
    /**
     * Sets a value on an entity only if the value is not null.
     * Useful for partial updates where null values should be ignored.
     * 
     * @param value The value to set (may be null)
     * @param setter The setter method reference
     * @param <T> The type of value
     */
    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * Updates entity title and description from a DTO if values are not null.
     * Common pattern for mappers with title/description fields.
     * 
     * @param dtoTitle The title from DTO
     * @param dtoDescription The description from DTO
     * @param titleSetter Entity title setter
     * @param descriptionSetter Entity description setter
     * @param <E> Entity type
     */
    public static <E> void updateTitleAndDescription(String dtoTitle, String dtoDescription,
                                                      Consumer<String> titleSetter,
                                                      Consumer<String> descriptionSetter) {
        setIfNotNull(dtoTitle, titleSetter);
        setIfNotNull(dtoDescription, descriptionSetter);
    }

    private MapperUtil() {
        // Utility class, no instantiation
    }
}

