import { useState, useEffect, useCallback } from "react";
import { TeacherService } from "../../teachers/services/teacherService";
import { SchoolService } from "../../schools/services/schoolService";
import { AllocationPlanService } from "../../allocation-plans/services/allocationPlanService";
import { TeacherFormSubmissionService } from "../../teacher-submissions/services/teacherFormSubmissionService";
import { CreditHourTrackingService } from "../../credit-hour-tracking/services/creditHourTrackingService";
import { calculateUtilization, calculatePlansByStatus } from "../utils/calculations";

// Constants
const DASHBOARD_PAGE_SIZE = 1000;

export interface AllocationUtilization {
  overUtilized: number; // creditBalance < 0
  underUtilized: number; // creditBalance > 0
  balanced: number; // creditBalance === 0
}

export interface AllocationPlansByStatus {
  draft: number;
  inReview: number;
  approved: number;
  archived: number;
}

export interface DashboardStats {
  teachers: { total: number };
  schools: { total: number };
  allocationPlans: { total: number };
  pendingSubmissions: { total: number };
  utilization: AllocationUtilization;
  plansByStatus: AllocationPlansByStatus;
}

export interface DashboardData {
  stats: DashboardStats | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useDashboard(): DashboardData {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboardData = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      // Fetch critical data first (these are required)
      const [teachersData, schoolsData, submissionsData] = await Promise.all([
        TeacherService.list({ page: 1, pageSize: 1 }),
        SchoolService.list({ page: 1, pageSize: 1 }),
        TeacherFormSubmissionService.list({ page: 1, pageSize: 1, isProcessed: false }),
      ]);

      // Fetch optional data with error handling (these can fail without breaking the dashboard)
      const [allocationPlansData, creditHourData] = await Promise.allSettled([
        AllocationPlanService.getPaginated({ page: 1, pageSize: DASHBOARD_PAGE_SIZE }),
        CreditHourTrackingService.list({ page: 1, pageSize: DASHBOARD_PAGE_SIZE }),
      ]);

      // Extract data with fallbacks
      const plansData = allocationPlansData.status === "fulfilled" 
        ? allocationPlansData.value 
        : { items: [], totalItems: 0 };
      
      const creditData = creditHourData.status === "fulfilled"
        ? creditHourData.value
        : { items: [] };

      // Calculate statistics using optimized single-pass functions
      const creditHours = creditData.items ?? [];
      const utilization = calculateUtilization(creditHours);

      const plans = plansData.items ?? [];
      const plansByStatus = calculatePlansByStatus(plans);

      setStats({
        teachers: { total: teachersData.totalItems || 0 },
        schools: { total: schoolsData.totalItems || 0 },
        allocationPlans: { total: plansData.totalItems || 0 },
        pendingSubmissions: { total: submissionsData.totalItems || 0 },
        utilization,
        plansByStatus,
      });
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load dashboard data";
      setError(message);
      console.error("Dashboard data loading error:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  return {
    stats,
    loading,
    error,
    refresh: loadDashboardData,
  };
}
