import { describe, it, expect } from "vitest";
import { buildAuditLogQueryParams } from "../queryParams";
import type { AuditLogFilters } from "../../types/auditLog.types";
import { AuditAction } from "../../types/auditLog.types";

describe("buildAuditLogQueryParams", () => {
  it("should build empty query params for empty filters", () => {
    const params = buildAuditLogQueryParams({});
    expect(params.toString()).toBe("");
  });

  it("should include userId in query params", () => {
    const filters: AuditLogFilters = { userId: 123 };
    const params = buildAuditLogQueryParams(filters);
    expect(params.get("userId")).toBe("123");
  });

  it("should include action in query params", () => {
    const filters: AuditLogFilters = { action: AuditAction.CREATE };
    const params = buildAuditLogQueryParams(filters);
    expect(params.get("action")).toBe(AuditAction.CREATE);
  });

  it("should include targetEntity in query params", () => {
    const filters: AuditLogFilters = { targetEntity: "User" };
    const params = buildAuditLogQueryParams(filters);
    expect(params.get("targetEntity")).toBe("User");
  });

  it("should include date filters in query params", () => {
    const filters: AuditLogFilters = {
      startDate: "2024-01-01T00:00:00Z",
      endDate: "2024-12-31T23:59:59Z",
    };
    const params = buildAuditLogQueryParams(filters);
    expect(params.get("startDate")).toBe("2024-01-01T00:00:00Z");
    expect(params.get("endDate")).toBe("2024-12-31T23:59:59Z");
  });

  it("should include pagination params", () => {
    const filters: AuditLogFilters = {
      page: 2,
      size: 50,
    };
    const params = buildAuditLogQueryParams(filters);
    expect(params.get("page")).toBe("2");
    expect(params.get("size")).toBe("50");
  });

  it("should include sort params", () => {
    const filters: AuditLogFilters = {
      sortBy: "eventTimestamp",
      sortDirection: "DESC",
    };
    const params = buildAuditLogQueryParams(filters);
    expect(params.get("sortBy")).toBe("eventTimestamp");
    expect(params.get("sortDirection")).toBe("DESC");
  });

  it("should include additional params", () => {
    const filters: AuditLogFilters = {};
    const params = buildAuditLogQueryParams(filters, {
      maxRecords: "1000",
      customParam: "value",
    });
    expect(params.get("maxRecords")).toBe("1000");
    expect(params.get("customParam")).toBe("value");
  });

  it("should build complete query string with all filters", () => {
    const filters: AuditLogFilters = {
      userId: 123,
      action: AuditAction.UPDATE,
      targetEntity: "User",
      startDate: "2024-01-01T00:00:00Z",
      endDate: "2024-12-31T23:59:59Z",
      page: 0,
      size: 20,
      sortBy: "eventTimestamp",
      sortDirection: "DESC",
    };
    const params = buildAuditLogQueryParams(filters);
    const queryString = params.toString();
    
    expect(queryString).toContain("userId=123");
    expect(queryString).toContain("action=UPDATE");
    expect(queryString).toContain("targetEntity=User");
    expect(queryString).toContain("startDate=");
    expect(queryString).toContain("endDate=");
    expect(queryString).toContain("page=0");
    expect(queryString).toContain("size=20");
    expect(queryString).toContain("sortBy=eventTimestamp");
    expect(queryString).toContain("sortDirection=DESC");
  });
});

