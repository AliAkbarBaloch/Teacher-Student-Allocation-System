import type { EmploymentStatus, UsageCycle } from "@/features/teachers/types/teacher.types";

/**
 * All available employment status options for teachers
 * Single source of truth for employment status values
 */
export const EMPLOYMENT_STATUS_OPTIONS: EmploymentStatus[] = [
  "FULL_TIME",
  "PART_TIME",
  "ON_LEAVE",
  "CONTRACT",
  "PROBATION",
  "RETIRED",
];

/**
 * All available usage cycle options for teachers
 * Single source of truth for usage cycle values
 */
export const USAGE_CYCLE_OPTIONS: UsageCycle[] = [
  "SEMESTER_1",
  "SEMESTER_2",
  "FULL_YEAR",
  "QUARTERLY",
];

