// Example usage in a page wrapper
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { apiClient } from "@/lib/api-client";
import { toast } from "sonner";
import AllocationReportView, {
  type AllocationReportData,
  type ApiResponse,
} from "@/features/reports/components/AllocationReportView";

export const API_BASE_URL = "http://localhost:8080/api";

export default function AllocationReportPage() {
  const { planId } = useParams<{ planId: string }>();
  const [data, setData] = useState<AllocationReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchReport = async () => {
      setLoading(true);
      setError(null);
      try {
        // Use specific planId from URL, or fetch latest current/approved plan
        const endpoint = planId
          ? `/reports/allocation/${planId}`
          : `/reports/allocation/latest`;
        const res = await apiClient.get<ApiResponse<AllocationReportData>>(
          endpoint
        );
        setData(res.data);
      } catch (err) {
        console.log(err);
        setError("Error loading report.");
      } finally {
        setLoading(false);
      }
    };
    fetchReport();
  }, [planId]);

  const handleExportExcel = async () => {
    if (!data) {
      toast.error("No report data available to export");
      return;
    }

    try {
      // Use planId from URL or data, or default to 1
      const id = planId || "1";

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

  if (loading) return <div>Loading report...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="container mx-auto py-6">
      {data && (
        <AllocationReportView data={data} onExport={handleExportExcel} />
      )}
    </div>
  );
}
