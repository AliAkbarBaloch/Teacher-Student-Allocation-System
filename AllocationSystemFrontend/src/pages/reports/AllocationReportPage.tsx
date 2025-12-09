// Example usage in a page wrapper
import { useEffect, useState } from "react";
import { apiClient } from "@/lib/api-client";
import AllocationReportView, { type AllocationReportData, type ApiResponse } from "@/features/reports/components/AllocationReportView";

export const API_BASE_URL = "http://localhost:8080/api";

export default function AllocationReportPage() {
  const [data, setData] = useState<AllocationReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  

  useEffect(() => {
    const fetchReport = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await apiClient.get<ApiResponse<AllocationReportData>>("/reports/allocation/1");
        setData(res.data);
      } catch (err) {
        console.log(err);
        setError("Error loading report.");
      } finally {
        setLoading(false);
      }
    };
    fetchReport();
  }, []);

  if (loading) return <div>Loading report...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="container mx-auto py-6">
      {data && <AllocationReportView data={data} onExport={() => {}} />}
    </div>
  );
}