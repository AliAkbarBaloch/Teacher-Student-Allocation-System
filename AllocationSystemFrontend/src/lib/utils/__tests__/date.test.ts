import { describe, it, expect } from "vitest";
import { formatDate, formatDateForInput, parseDateInput } from "../date";

describe("formatDate", () => {
  it("should format valid ISO date string", () => {
    const dateString = "2024-01-15T10:30:00Z";
    const result = formatDate(dateString);
    expect(result).toBeTruthy();
    expect(typeof result).toBe("string");
  });

  it("should return original string for invalid date", () => {
    const invalidDate = "invalid-date";
    const result = formatDate(invalidDate);
    expect(result).toBe(invalidDate);
  });

  it("should handle empty string", () => {
    const result = formatDate("");
    expect(result).toBe("");
  });
});

describe("formatDateForInput", () => {
  it("should format valid ISO date string for datetime-local input", () => {
    const dateString = "2024-01-15T10:30:00Z";
    const result = formatDateForInput(dateString);
    expect(result).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
  });

  it("should return empty string for null", () => {
    const result = formatDateForInput(null);
    expect(result).toBe("");
  });

  it("should return empty string for undefined", () => {
    const result = formatDateForInput(undefined);
    expect(result).toBe("");
  });

  it("should return empty string for invalid date", () => {
    const result = formatDateForInput("invalid-date");
    expect(result).toBe("");
  });
});

describe("parseDateInput", () => {
  it("should parse valid datetime-local input to ISO string", () => {
    const input = "2024-01-15T10:30";
    const result = parseDateInput(input);
    expect(result).toBeTruthy();
    expect(result).toContain("2024-01-15");
  });

  it("should return undefined for null", () => {
    const result = parseDateInput(null);
    expect(result).toBeUndefined();
  });

  it("should return undefined for empty string", () => {
    const result = parseDateInput("");
    expect(result).toBeUndefined();
  });

  it("should return undefined for invalid input", () => {
    const result = parseDateInput("invalid");
    expect(result).toBeUndefined();
  });
});

