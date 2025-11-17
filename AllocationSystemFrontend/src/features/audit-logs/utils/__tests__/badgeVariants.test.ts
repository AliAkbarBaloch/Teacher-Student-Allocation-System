import { describe, it, expect } from "vitest";
import { getActionBadgeVariant } from "../badgeVariants";
import { AuditAction } from "../../types/auditLog.types";

describe("getActionBadgeVariant", () => {
  it("should return 'default' for CREATE actions", () => {
    expect(getActionBadgeVariant(AuditAction.CREATE)).toBe("default");
    expect(getActionBadgeVariant(AuditAction.PASSWORD_RESET)).toBe("default");
  });

  it("should return 'secondary' for UPDATE actions", () => {
    expect(getActionBadgeVariant(AuditAction.UPDATE)).toBe("secondary");
    expect(getActionBadgeVariant(AuditAction.PROFILE_UPDATED)).toBe("secondary");
    expect(getActionBadgeVariant(AuditAction.PERMISSION_CHANGE)).toBe("secondary");
  });

  it("should return 'destructive' for DELETE actions", () => {
    expect(getActionBadgeVariant(AuditAction.DELETE)).toBe("destructive");
    expect(getActionBadgeVariant(AuditAction.ACCOUNT_LOCKED)).toBe("destructive");
  });

  it("should return 'outline' for LOGIN and LOGOUT", () => {
    expect(getActionBadgeVariant(AuditAction.LOGIN)).toBe("outline");
    expect(getActionBadgeVariant(AuditAction.LOGOUT)).toBe("outline");
  });

  it("should return 'outline' for other actions", () => {
    expect(getActionBadgeVariant(AuditAction.VIEW)).toBe("outline");
    expect(getActionBadgeVariant(AuditAction.EXPORT)).toBe("outline");
    expect(getActionBadgeVariant(AuditAction.IMPORT)).toBe("outline");
  });
});

