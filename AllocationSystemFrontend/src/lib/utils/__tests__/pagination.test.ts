import { describe, it, expect } from "vitest";
import { getVisiblePages, getPaginationSummary, clampPage } from "../pagination";

describe("pagination utils", () => {
    describe("getVisiblePages", () => {
        it("returns all pages when total pages is small", () => {
            expect(getVisiblePages(1, 3)).toEqual([1, 2, 3]);
        });

        it("respects the range around current page", () => {
            // current=5, total=10, range=2 => [3,4,5,6,7]
            expect(getVisiblePages(5, 10, 2)).toEqual([3, 4, 5, 6, 7]);
        });

        it("clamps to start", () => {
            // current=1, total=10, range=2 => [1,2,3] (start can't be < 1)
            expect(getVisiblePages(1, 10, 2)).toEqual([1, 2, 3]);
        });

        it("clamps to end", () => {
            // current=10, total=10, range=2 => [8,9,10] (end can't be > 10)
            expect(getVisiblePages(10, 10, 2)).toEqual([8, 9, 10]);
        });

        it("handles empty total pages", () => {
            expect(getVisiblePages(1, 0)).toEqual([1]);
        });
    });

    describe("getPaginationSummary", () => {
        it("returns correct summary for first page", () => {
            // page 1, size 10, total 25 => 1-10
            expect(getPaginationSummary(1, 10, 25)).toEqual({ from: 1, to: 10 });
        });

        it("returns correct summary for middle page", () => {
            // page 2, size 10, total 25 => 11-20
            expect(getPaginationSummary(2, 10, 25)).toEqual({ from: 11, to: 20 });
        });

        it("returns correct summary for last page", () => {
            // page 3, size 10, total 25 => 21-25
            expect(getPaginationSummary(3, 10, 25)).toEqual({ from: 21, to: 25 });
        });

        it("handles zero items", () => {
            expect(getPaginationSummary(1, 10, 0)).toEqual({ from: 0, to: 0 });
        });
    });

    describe("clampPage", () => {
        it("clamps negative page to 1", () => {
            expect(clampPage(-1, 5)).toBe(1);
        });

        it("clamps page greater than total to total", () => {
            expect(clampPage(10, 5)).toBe(5);
        });

        it("returns valid page as is", () => {
            expect(clampPage(3, 5)).toBe(3);
        });
    });
});
