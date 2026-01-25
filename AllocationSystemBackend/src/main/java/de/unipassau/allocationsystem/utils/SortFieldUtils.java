package de.unipassau.allocationsystem.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting field names to sortable field metadata.
 * Generates sort field options with formatted labels.
 */
public class SortFieldUtils {
    /**
     * Converts field names to a list of sort field options.
     * Each option contains a key (field name) and a human-readable label.
     * 
     * @param fields the field names to convert
     * @return list of sort field options as key-label maps
     */
    public static List<Map<String, String>> getSortFields(String... fields) {
        List<Map<String, String>> result = new ArrayList<>();
        for (String field : fields) {
            result.add(Map.of("key", field, "label", toLabel(field)));
        }
        return result;
    }

    private static String toLabel(String field) {
        // Converts camelCase or snake_case to Title Case label
        String label = field.replaceAll("_", " ");
        label = label.replaceAll("([a-z])([A-Z])", "$1 $2");
        label = label.substring(0, 1).toUpperCase() + label.substring(1);
        return label;
    }
}
