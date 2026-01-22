package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.User;

/**
 * Immutable query parameters for listing users with filtering, search, pagination and sorting.
 *
 * @param role          optional role filter
 * @param status        optional account status filter
 * @param enabled       optional enabled filter
 * @param search        optional free-text search on email/fullName
 * @param page          zero-based page index
 * @param size          page size
 * @param sortBy        field name to sort by
 * @param sortDirection sort direction (e.g. ASC/DESC)
 */
public record UserQuery(
        User.UserRole role,
        User.AccountStatus status,
        Boolean enabled,
        String search,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {

    /**
     * Builder for {@link UserQuery}. Avoids constructors with many parameters and supports defaults.
     */
    public static final class Builder {
        private User.UserRole role;
        private User.AccountStatus status;
        private Boolean enabled;
        private String search;
        private int page;
        private int size;
        private String sortBy = "id";
        private String sortDirection = "ASC";

        /**
         * Sets the role filter.
         *
         * @param role role to filter by
         * @return builder
         */
        public Builder role(User.UserRole role) {
            this.role = role;
            return this;
        }

        /**
         * Sets the account status filter.
         *
         * @param status account status to filter by
         * @return builder
         */
        public Builder status(User.AccountStatus status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the enabled filter.
         *
         * @param enabled enabled flag to filter by
         * @return builder
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the free-text search term.
         *
         * @param search search term (email/fullName)
         * @return builder
         */
        public Builder search(String search) {
            this.search = search;
            return this;
        }

        /**
         * Sets the page index (zero-based).
         *
         * @param page page index
         * @return builder
         */
        public Builder page(int page) {
            this.page = page;
            return this;
        }

        /**
         * Sets the page size.
         *
         * @param size page size
         * @return builder
         */
        public Builder size(int size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the field to sort by.
         *
         * @param sortBy field name
         * @return builder
         */
        public Builder sortBy(String sortBy) {
            if (sortBy != null && !sortBy.trim().isEmpty()) {
                this.sortBy = sortBy;
            }
            return this;
        }

        /**
         * Sets the sort direction.
         *
         * @param sortDirection sort direction (ASC/DESC)
         * @return builder
         */
        public Builder sortDirection(String sortDirection) {
            if (sortDirection != null && !sortDirection.trim().isEmpty()) {
                this.sortDirection = sortDirection;
            }
            return this;
        }

        /**
         * Builds a {@link UserQuery} instance.
         *
         * @return new immutable {@link UserQuery}
         */
        public UserQuery build() {
            return new UserQuery(role, status, enabled, search, page, size, sortBy, sortDirection);
        }
    }
}
