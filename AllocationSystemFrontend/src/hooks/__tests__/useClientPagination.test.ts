import { renderHook, act } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { useClientPagination } from "../useClientPagination";

describe("useClientPagination", () => {
    const mockData = Array.from({ length: 25 }, (_, i) => ({ id: i + 1, name: `Item ${i + 1}` }));

    it("initializes with first page", () => {
        const { result } = renderHook(() => useClientPagination({ data: mockData, pageSize: 10 }));

        expect(result.current.currentPage).toBe(1);
        expect(result.current.totalPages).toBe(3);
        expect(result.current.paginatedData).toHaveLength(10);
        expect(result.current.paginatedData[0].id).toBe(1);
        expect(result.current.paginatedData[9].id).toBe(10);
    });

    it("navigates to next page", () => {
        const { result } = renderHook(() => useClientPagination({ data: mockData, pageSize: 10 }));

        act(() => {
            result.current.handleNextPage();
        });

        expect(result.current.currentPage).toBe(2);
        expect(result.current.paginatedData).toHaveLength(10);
        expect(result.current.paginatedData[0].id).toBe(11);
    });

    it("navigates to previous page", () => {
        const { result } = renderHook(() => useClientPagination({ data: mockData, pageSize: 10 }));

        act(() => {
            result.current.setCurrentPage(2);
        });

        act(() => {
            result.current.handlePreviousPage();
        });

        expect(result.current.currentPage).toBe(1);
        expect(result.current.paginatedData[0].id).toBe(1);
    });

    it("clamps next page at total pages", () => {
        const { result } = renderHook(() => useClientPagination({ data: mockData, pageSize: 10 }));

        // Go to last page (3)
        act(() => {
            result.current.setCurrentPage(3);
        });

        // Try next
        act(() => {
            result.current.handleNextPage();
        });

        expect(result.current.currentPage).toBe(3);
    });

    it("clamps previous page at 1", () => {
        const { result } = renderHook(() => useClientPagination({ data: mockData, pageSize: 10 }));

        act(() => {
            result.current.handlePreviousPage();
        });

        expect(result.current.currentPage).toBe(1);
    });

    it("resets to page 1 when data changes significantly (filtering)", () => {
        const { result, rerender } = renderHook(
            (props) => useClientPagination(props),
            { initialProps: { data: mockData, pageSize: 10 } }
        );

        // Go to page 3
        act(() => {
            result.current.setCurrentPage(3);
        });
        expect(result.current.currentPage).toBe(3);

        // Update data to only have 5 items (1 page)
        const filteredData = mockData.slice(0, 5);
        rerender({ data: filteredData, pageSize: 10 });

        expect(result.current.currentPage).toBe(1);
    });
});
