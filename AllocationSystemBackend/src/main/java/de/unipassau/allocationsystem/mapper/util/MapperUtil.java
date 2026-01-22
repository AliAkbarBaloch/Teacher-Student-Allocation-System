package de.unipassau.allocationsystem.mapper.util;

import java.util.function.Consumer;

/**
 * Utility for common mapper operations.
 * Provides reusable helper methods for entity updates and DTO operations.
 */
public class MapperUtil {
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

    private MapperUtil() {
        // Utility class, no instantiation
    }
}
