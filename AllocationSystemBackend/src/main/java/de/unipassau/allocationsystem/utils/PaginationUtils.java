package de.unipassau.allocationsystem.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling pagination parameters and formatting responses.
 */
public class PaginationUtils {

    /**
     * Record holding pagination parameters.
     * 
     * @param page the page number (1-indexed)
     * @param pageSize the number of items per page
     * @param sortBy the field name to sort by
     * @param sortOrder the sort direction (ASC or DESC)
     */
    public record PaginationParams(int page, int pageSize, String sortBy, Sort.Direction sortOrder) {
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Validates and normalizes pagination parameters from query params.
     * Applies default values and constraints (max page size, min values).
     * 
     * @param queryParams the query parameters map
     * @return validated pagination parameters
     */
    public static PaginationParams validatePaginationParams(Map<String, String> queryParams) {
        int page = 1;
        int pageSize = DEFAULT_PAGE_SIZE;
        String sortBy = "id";
        Sort.Direction sortOrder = Sort.Direction.DESC;

        if (queryParams.containsKey("page")) {
            try {
                page = Math.max(1, Integer.parseInt(queryParams.get("page")));
            } catch (NumberFormatException e) {
                // Use default page value
            }
        }

        if (queryParams.containsKey("pageSize")) {
            try {
                int requestedPageSize = Integer.parseInt(queryParams.get("pageSize"));
                pageSize = Math.max(1, Math.min(requestedPageSize, MAX_PAGE_SIZE));
            } catch (NumberFormatException ignored) {
                pageSize = DEFAULT_PAGE_SIZE;
            }
        }

        if (queryParams.containsKey("sortBy")) {
            sortBy = queryParams.get("sortBy");
        }

        if (queryParams.containsKey("sortOrder") && queryParams.get("sortOrder") != null) {
            String so = queryParams.get("sortOrder").toLowerCase();
            if ("asc".equals(so)) {
                sortOrder = Sort.Direction.ASC;
            } else if ("desc".equals(so)) {
                sortOrder = Sort.Direction.DESC;
            }
        }

        return new PaginationParams(page, pageSize, sortBy, sortOrder);
    }

    /**
     * Formats a Spring Data Page object into a standard pagination response.
     * 
     * @param page the page object containing items and metadata
     * @return map containing items, total counts, and pagination info
     */
    public static Map<String, Object> formatPaginationResponse(Page<?> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("items", page.getContent());
        result.put("totalItems", page.getTotalElements());
        result.put("totalPages", page.getTotalPages());
        result.put("page", page.getNumber() + 1);
        result.put("pageSize", page.getSize());
        return result;
    }
}
