import type { EmploymentStatus, UsageCycle } from "@/features/teachers/types/teacher.types";

/**
 * All available employment status options for teachers
 * Single source of truth for employment status values
 */
export const EMPLOYMENT_STATUS_OPTIONS: EmploymentStatus[] = [
  "ACTIVE", // Available for assignment
  "INACTIVE_THIS_YEAR", // Marked "nicht" for this specific year
  "ON_LEAVE", // Sabbatical/Parental leave
  "ARCHIVED", // No longer in the system (Retired/Left)
];

/**
 * All available usage cycle options for teachers
 * Single source of truth for usage cycle values
 */
export const USAGE_CYCLE_OPTIONS: UsageCycle[] = [
  "GRADES_1_2",
  "GRADES_3_4",
  "GRADES_5_TO_9",
  "FLEXIBLE",
];

