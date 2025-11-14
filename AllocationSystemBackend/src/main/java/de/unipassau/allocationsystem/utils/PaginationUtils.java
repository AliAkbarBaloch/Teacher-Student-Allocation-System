package de.unipassau.allocationsystem.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

public class PaginationUtils {

    public record PaginationParams(int page, int pageSize, String sortBy, Sort.Direction sortOrder) {
    }

    public static PaginationParams validatePaginationParams(Map<String, String> queryParams) {
        int page = 1;
        int pageSize = 10;
        String sortBy = "id";
        Sort.Direction sortOrder = Sort.Direction.DESC;

        if (queryParams.containsKey("page")) {
            try {
                page = Math.max(1, Integer.parseInt(queryParams.get("page")));
            } catch (NumberFormatException ignored) {

            }
        }

        if (queryParams.containsKey("pageSize")) {
            try {
                pageSize = Math.max(1, Integer.parseInt(queryParams.get("pageSize")));
            } catch (NumberFormatException ignored) {

            }
        }

        if (queryParams.containsKey("sortBy")) {
            sortBy = queryParams.get("sortBy");
        }

        if (queryParams.containsKey("sortOrder")) {
            String so = queryParams.get("sortOrder").toLowerCase();
            if ("asc".equals(so)) {
                sortOrder = Sort.Direction.ASC;
            } else if ("desc".equals(so)) {
                sortOrder = Sort.Direction.DESC;
            }
        }

        return new PaginationParams(page, pageSize, sortBy, sortOrder);
    }

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
