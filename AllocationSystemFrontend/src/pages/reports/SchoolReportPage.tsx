import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import SchoolStatusReportView, { type SchoolStatusReportData } from "@/features/reports/components/SchoolStatusReportView";
import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/services/api/BaseApiService";
import { useQuery } from "@tanstack/react-query";
import { AlertCircle, RefreshCw } from "lucide-react";

export default function SchoolReportPage() {
  // Fetch data using React Query
  const { 
    data, 
    isLoading, 
    isError, 
    error, 
    refetch 
  } = useQuery({
    queryKey: ["school-status-report"],
    queryFn: async () => {
      // Calls: GET /api/reports/schools/status
      const response = await apiClient.get<ApiResponse<SchoolStatusReportData>>("/reports/schools/status");
      return response.data; // Adapts to your API response structure { success: true, data: ... }
    },
    // Optional: Keep data fresh for 5 minutes, as school data doesn't change often
    staleTime: 5 * 60 * 1000, 
  });

  // 1. Loading State
  if (isLoading) {
    return (
      <div className="container mx-auto py-8 px-4 space-y-8">
        <div className="space-y-2">
          <Skeleton className="h-8 w-1/3" />
          <Skeleton className="h-4 w-1/2" />
        </div>
        {/* Metric Cards Skeleton */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-32 w-full rounded-lg" />
          ))}
        </div>
        {/* Charts Skeleton */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-[300px] w-full rounded-lg" />
          ))}
        </div>
        {/* List Skeleton */}
        <Skeleton className="h-[400px] w-full rounded-lg" />
      </div>
    );
  }

  // 2. Error State
  if (isError) {
    return (
      <div className="container mx-auto py-12 px-4">
        <Card className="border-destructive/50 bg-destructive/5 max-w-2xl mx-auto">
          <CardContent className="pt-6 text-center space-y-4">
            <div className="bg-destructive/10 w-12 h-12 rounded-full flex items-center justify-center mx-auto text-destructive">
              <AlertCircle className="h-6 w-6" />
            </div>
            <h2 className="text-xl font-semibold text-destructive">
              Failed to load school report
            </h2>
            <p className="text-muted-foreground">
              {(error as Error)?.message || "An unexpected error occurred while fetching the data."}
            </p>
            <Button onClick={() => refetch()} variant="outline" className="mt-4">
              <RefreshCw className="mr-2 h-4 w-4" /> Try Again
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  // 3. Success State
  return (
    <div className="container mx-auto py-8 px-4 min-h-screen bg-slate-50/50">
      {/* Page Header */}
      <div className="mb-8 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">
            School Network Overview
          </h1>
          <p className="text-muted-foreground mt-1">
            Analytics on school distribution, zoning, transport accessibility, and teacher capacity.
          </p>
        </div>
        
        <div className="flex gap-2">
           <Button variant="outline" size="sm" onClick={() => refetch()}>
             <RefreshCw className="mr-2 h-4 w-4" /> Refresh Data
           </Button>
        </div>
      </div>

      {/* The Report Component */}
      {data &&
        <SchoolStatusReportView data={data} />
      }
    </div>
  );
}