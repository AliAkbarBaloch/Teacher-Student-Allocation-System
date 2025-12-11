package de.unipassau.allocationsystem.utils;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Path;

public class SearchSpecificationUtils {
    public static <T> Specification<T> buildLikeSpecification(String field, String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), likePattern);
    }

    public static <T> Specification<T> buildMultiFieldLikeSpecification(String[] fields, String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty() || fields == null || fields.length == 0) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            Predicate[] predicates = Arrays.stream(fields)
                .map(field -> {
                    Path<?> path = getPath(root, field);
                    return cb.like(cb.lower(path.as(String.class)), likePattern);
                })
                .toArray(Predicate[]::new);
            return cb.or(predicates);
        };
    }

    private static Path<?> getPath(Path<?> root, String field) {
        if (field.contains(".")) {
            String[] parts = field.split("\\.");
            Path<?> path = root;
            for (String part : parts) {
                path = path.get(part);
            }
            return path;
        }
        return root.get(field);
    }
}
