/**
 * Common pagination and filtering parameters used by most services.
 */
export interface PaginationParams {
    page?: number;
    pageSize?: number;
    sortBy?: string;
    sortOrder?: string;
    searchValue?: string;
    [key: string]: string | number | boolean | undefined;
}

/**
 * Builds URLSearchParams from a params object, handling common pagination fields
 * and any additional key-value pairs.
 * 
 * @param params - The object containing query parameters
 * @returns A URLSearchParams object
 */
export function buildQueryParams(params: Record<string, string | number | boolean | undefined | null> = {}): URLSearchParams {
    const queryParams = new URLSearchParams();

    if (!params) return queryParams;

    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            queryParams.append(key, String(value));
        }
    });

    return queryParams;
}
