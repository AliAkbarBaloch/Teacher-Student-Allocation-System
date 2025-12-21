// Example usage in a page wrapper
import AllocationReportView, {
  type AllocationReportData,
  type ApiResponse,
} from "@/features/reports/components/AllocationReportView";
import useAllocationPlans from "@/hooks/entities/useAllocationPlans";
import { apiClient } from "@/lib/api-client";
import { useEffect, useState } from "react";
import { toast } from "sonner";

export const API_BASE_URL = "http://localhost:8080/api";

export default function AllocationReportPage() {
  const { data: allocationPlans, isLoading: isAllocationPlanLoading } = useAllocationPlans();
  const [selectedPlan, setSelectedPlan] = useState<string | undefined>(undefined);

  const [data, setData] = useState<AllocationReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Set default plan when allocation plans are loaded
  useEffect(() => {
    if (!selectedPlan && allocationPlans && allocationPlans.length > 0) {
      setSelectedPlan(String(allocationPlans[0].id));
    }
  }, [selectedPlan, allocationPlans]);

  useEffect(() => {
    const fetchReport = async () => {
      setLoading(true);
      setError(null);
      try {
        const endpoint = selectedPlan
          ? `/reports/allocation/${selectedPlan}`
          : `/reports/allocation/latest`;
        const res = await apiClient.get<ApiResponse<AllocationReportData>>(endpoint);
        setData(res.data);
      } catch (err) {
        console.log(err);
        setError("Error loading report.");
      } finally {
        setLoading(false);
      }
    };
    if (selectedPlan) fetchReport();
  }, [selectedPlan]);

  const handlePlanChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedPlan(e.target.value);
  };

  const handleExportExcel = async () => {
    if (!data) {
      toast.error("No report data available to export");
      return;
    }

    try {
      const id = selectedPlan || "1";
      const token = localStorage.getItem("auth_token");
      if (!token) {
        toast.error("Authentication required. Please log in.");
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
        console.error("Excel export error:", errorText);
        throw new Error(
          `Failed to download report: ${response.status} ${response.statusText}`
        );
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `allocation_report_${id}_${
        new Date().toISOString().split("T")[0]
      }.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      toast.success("Excel report downloaded successfully");
    } catch (err) {
      console.error("Error downloading Excel report:", err);
      const errorMessage =
        err instanceof Error ? err.message : "Failed to download Excel report";
      setError(errorMessage);
      toast.error(errorMessage);
    }
  };

  if (loading || isAllocationPlanLoading || !allocationPlans) return <div>Loading report...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="container mx-auto py-6">
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Allocation Report</h1>
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
      {data && (
        <AllocationReportView data={data} onExport={handleExportExcel} />
      )}
    </div>
  );
}
