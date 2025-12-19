import type { CreditHourTracking } from "../../credit-hour-tracking/types/creditHourTracking.types";
import type { AllocationPlan } from "../../allocation-plans/types/allocationPlan.types";
import type { AllocationUtilization, AllocationPlansByStatus } from "../hooks/useDashboard";

/**
 * Calculate utilization statistics from credit hour tracking data
 * Single pass through the array for better performance
 */
export function calculateUtilization(
  creditHours: CreditHourTracking[]
): AllocationUtilization {
  return creditHours.reduce(
    (acc, ch) => {
      if (ch.creditBalance < 0) acc.overUtilized++;
      else if (ch.creditBalance > 0) acc.underUtilized++;
      else acc.balanced++;
      return acc;
    },
    { overUtilized: 0, underUtilized: 0, balanced: 0 }
  );
}

/**
 * Calculate allocation plans by status
 * Single pass through the array for better performance
 */
export function calculatePlansByStatus(
  plans: AllocationPlan[]
): AllocationPlansByStatus {
  return plans.reduce(
    (acc, plan) => {
      switch (plan.status) {
        case "DRAFT":
          acc.draft++;
          break;
        case "IN_REVIEW":
          acc.inReview++;
          break;
        case "APPROVED":
          acc.approved++;
          break;
        case "ARCHIVED":
          acc.archived++;
          break;
      }
      return acc;
    },
    { draft: 0, inReview: 0, approved: 0, archived: 0 }
  );
}

/**
 * Calculate percentage safely (handles division by zero)
 */
export function calculatePercentage(value: number, total: number): number {
  return total > 0 ? Math.round((value / total) * 100) : 0;
}
