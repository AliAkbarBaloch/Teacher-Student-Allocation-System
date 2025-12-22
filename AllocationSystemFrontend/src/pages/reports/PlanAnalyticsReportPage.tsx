import { Skeleton } from "@/components/ui/skeleton";
import PlanAnalyticsView, { type AllocationHealthReportDto, type SubjectBottleneckDto, type TeacherUtilizationReportDto } from "@/features/reports/components/PlanAnalyticsView";
import useAllocationPlans from "@/hooks/entities/useAllocationPlans";
import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/services/api/BaseApiService";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";

export default function PlanAnalyticsReportPage() {
  const { data: allocationPlans, isLoading: isAllocationPlanLoading } = useAllocationPlans();
  const [selectedPlan, setSelectedPlan] = useState<string | undefined>(undefined);

  // Set default plan when allocation plans are loaded
  useEffect(() => {
    if (!selectedPlan && allocationPlans && allocationPlans.length > 0) {
      setSelectedPlan(String(allocationPlans[0].id));
    }
  }, [selectedPlan, allocationPlans]);

  // Fetch all analytics data for the selected plan
  const healthQuery = useQuery({
    queryKey: ["report-health", selectedPlan],
    enabled: !!selectedPlan,
    queryFn: async () =>
      (await apiClient.get<ApiResponse<AllocationHealthReportDto>>(`/reports/plan/${selectedPlan}/health`)).data,
  });

  const bottleneckQuery = useQuery({
    queryKey: ["report-bottlenecks", selectedPlan],
    enabled: !!selectedPlan,
    queryFn: async () =>
      (await apiClient.get<ApiResponse<SubjectBottleneckDto[]>>(`/reports/plan/${selectedPlan}/bottlenecks`)).data,
  });

  const utilizationQuery = useQuery({
    queryKey: ["report-utilization", selectedPlan],
    enabled: !!selectedPlan,
    queryFn: async () =>
      (await apiClient.get<ApiResponse<TeacherUtilizationReportDto[]>>(`/reports/plan/${selectedPlan}/utilization`)).data,
  });

  const isLoading =
    isAllocationPlanLoading ||
    !allocationPlans ||
    healthQuery.isLoading ||
    bottleneckQuery.isLoading ||
    utilizationQuery.isLoading;

  const isError = healthQuery.isError || bottleneckQuery.isError || utilizationQuery.isError;

  const handlePlanChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedPlan(e.target.value);
  };

  if (isLoading) {
    return (
      <div className="container mx-auto py-8">
        <Skeleton className="h-12 w-1/3 mb-4" />
        <Skeleton className="h-[500px] w-full" />
      </div>
    );
  }

  if (isError) {
    return <div className="p-8 text-red-500">Failed to load analytics data.</div>;
  }

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Plan Analytics Report</h1>
          <p className="text-muted-foreground mt-1">
            Plan Analytics Report
          </p>
        </div>
        <div>
          <label htmlFor="allocationPlan" className="mr-2 font-medium">Allocation Plan:</label>
          <select
            id="allocationPlan"
            value={selectedPlan}
            onChange={handlePlanChange}
            className="border rounded px-2 py-1"
          >
            {allocationPlans.map((record) => (
              <option key={record.id} value={record.id}>
                { record.planName } ({record.planVersion}) {record.status}
              </option>
            ))}
          </select>
        </div>
      </div>
      {healthQuery.data && bottleneckQuery.data && utilizationQuery.data && (
        <PlanAnalyticsView
          healthData={healthQuery.data}
          bottleneckData={bottleneckQuery.data}
          utilizationData={utilizationQuery.data}
        />
      )}
    </div>
  );
}