import AllocationReportView, {
  type AllocationReportData,
} from "@/features/reports/components/AllocationReportView";
import AllocationRunModal from "@/features/reports/components/AllocationRunModal"; // Import the new modal
import useAllocationPlans from "@/hooks/entities/useAllocationPlans";
import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/services/api/BaseApiService";
import { useEffect, useState, useCallback } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { Button } from "@/components/ui/button"; // Assuming UI components exist
import { PlusCircle, RefreshCw } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";

export const API_BASE_URL = "http://localhost:8080/api";

export default function AllocationReportPage() {
  const { t } = useTranslation("reportPlanAnalytics");
  const queryClient = useQueryClient();

  // Allocation Plans Data
  const { data: allocationPlans, isLoading: isAllocationPlanLoading, refetch: refetchPlans } = useAllocationPlans();

  // State
  const [selectedPlan, setSelectedPlan] = useState<string | undefined>(undefined);
  const [data, setData] = useState<AllocationReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal State
  const [isRunModalOpen, setIsRunModalOpen] = useState(false);

  // Set default plan when allocation plans are loaded
  useEffect(() => {
    if (!selectedPlan && allocationPlans && allocationPlans.length > 0) {
      // Prefer the "Current" plan if exists, otherwise first
      const current = allocationPlans.find(p => p.isCurrent);
      setSelectedPlan(String(current ? current.id : allocationPlans[0].id));
    }
  }, [selectedPlan, allocationPlans]);

  // Fetch Report Data
  const fetchReport = useCallback(async () => {
    if (!selectedPlan) return;

    setLoading(true);
    setError(null);
    try {
      const endpoint = `/reports/allocation/${selectedPlan}`;
      const res = await apiClient.get<ApiResponse<AllocationReportData>>(endpoint);
      setData(res.data);
    } catch (err) {
      console.log(err);
      setError(t("errorLoadingReport"));
    } finally {
      setLoading(false);
    }
  }, [selectedPlan, t]);

  useEffect(() => {
    fetchReport();
  }, [fetchReport]);

  const handlePlanChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedPlan(e.target.value);
  };

  // --- Handlers for New Feature ---

  const handleRunSuccess = (newPlanId: number) => {
    // 1. Invalidate plans cache so the new plan shows up in dropdown
    queryClient.invalidateQueries({ queryKey: ["allocationPlans"] });
    refetchPlans();

    // 2. Select the newly created plan
    setSelectedPlan(String(newPlanId));
  };

  const handleExportExcel = async () => {
    if (!data) {
      toast.error(t("noDataToExport"));
      return;
    }

    try {
      const id = selectedPlan || "1";
      const token = localStorage.getItem("auth_token");
      if (!token) {
        toast.error(t("authRequired"));
        return;
      }

      const response = await fetch(
        `http://localhost:8080/api/reports/allocation-export/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorText = await response.text();
        console.error(t("excelExportError", { error: errorText }));
        throw new Error(
          `Failed to download report: ${response.status} ${response.statusText}`
        );
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `allocation_report_${id}_${new Date().toISOString().split("T")[0]
        }.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      toast.success(t("exportSuccess"));
    } catch (err) {
      console.error(t("exportError"), err);
      const errorMessage =
        err instanceof Error ? err.message : "Failed to download Excel report";
      setError(errorMessage);
      toast.error(errorMessage);
    }
  };

  if (isAllocationPlanLoading) return <div className="p-10 flex justify-center">{t("loading")}</div>;

  return (
    <div className="container mx-auto py-6">
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{t("title")}</h1>
          <p className="text-muted-foreground">{t("Analyze and manage teacher allocations")}</p>
        </div>

        <div className="flex flex-col sm:flex-row items-end sm:items-center gap-3">
          {/* Plan Selector */}
          <div className="flex flex-col sm:flex-row sm:items-center gap-2">
            <label htmlFor="allocationPlan" className="font-medium text-sm">{t("allocationPlanLabel")}</label>
            <div className="flex items-center gap-2">
              <select
                id="allocationPlan"
                value={selectedPlan || ""}
                onChange={handlePlanChange}
                className="h-10 w-[300px] rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2"
                disabled={!allocationPlans || allocationPlans.length === 0}
              >
                {allocationPlans?.map((record) => (
                  <option key={record.id} value={record.id}>
                    {record.isCurrent ? "★ " : ""}{record.planName} ({record.planVersion}) - {record.status}
                  </option>
                ))}
              </select>
              <Button
                variant="ghost"
                size="icon"
                onClick={() => { refetchPlans(); fetchReport(); }}
                title="Refresh Data"
              >
                <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
              </Button>
            </div>
          </div>

          {/* NEW: Generate Button */}
          <Button onClick={() => setIsRunModalOpen(true)} className="gap-2">
            <PlusCircle className="h-4 w-4" />
            {t("Generate Allocation")}
          </Button>
        </div>
      </div>

      {loading && <div className="py-10 text-center">{t("loading")}</div>}

      {error && (
        <div className="p-6 bg-red-50 text-red-600 rounded-lg border border-red-200">
          {error}
        </div>
      )}

      {/* Report View */}
      {!loading && !error && data && (
        <AllocationReportView data={data} onExport={handleExportExcel} />
      )}

      {!loading && !error && !data && selectedPlan && (
        <div className="text-center py-10 text-muted-foreground">{t("No report data found for this plan.")}</div>
      )}

      {/* Reusable Modal Component */}
      <AllocationRunModal
        isOpen={isRunModalOpen}
        onClose={() => setIsRunModalOpen(false)}
        onSuccess={handleRunSuccess}
      />
    </div>
  );
}