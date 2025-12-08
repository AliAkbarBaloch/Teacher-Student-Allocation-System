/**
 * Constants for Credit Hour Tracking feature
 */

// Balance threshold constants for conditional formatting
export const CREDIT_BALANCE_THRESHOLDS = {
  /** Balance above this value is considered high (under-utilized) - shown in green */
  HIGH: 50,
  /** Balance above this value is considered moderate - shown in yellow */
  MODERATE: 20,
  /** Balance below 0 is considered negative (over-utilized) - shown in red */
  NEGATIVE: 0,
} as const;

// Input validation constants
export const BALANCE = 0;

// Notes field constraints
export const NOTES_CONSTRAINTS = {
  MAX_LENGTH: 1000,
  DISPLAY_TRUNCATE_LENGTH: 50,
} as const;
