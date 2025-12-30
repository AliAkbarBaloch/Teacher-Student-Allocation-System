import { Skeleton } from "@/components/ui/skeleton";
import TeacherStatusReportView, { type TeacherStatusReportData } from "@/features/reports/components/TeacherStatusReportView";
import useAcademicYears from "@/hooks/entities/useAcademicYears";
import { apiClient } from "@/lib/api-client";
import type { ApiResponse } from "@/services/api/BaseApiService";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";

export default function TeacherReportPage() {
  const { t } = useTranslation("reportTeachers");

  const { data: academicYears, isLoading: isAcademicYearLoading } = useAcademicYears();
  const [selectedYear, setSelectedYear] = useState<string | undefined>(undefined);
  const [data, setData] = useState<TeacherStatusReportData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Set default year when academic years are loaded
  useEffect(() => {
    if (!selectedYear && academicYears && academicYears.length > 0) {
      setSelectedYear(String(academicYears[0].id));
    }
  }, [selectedYear, academicYears]);

  useEffect(() => {
    if (!selectedYear) return;
    const fetchReport = async () => {
      setLoading(true);
      setError(null);
      try {
        const endpoint = `/reports/teachers/status?academicYearId=${selectedYear}`;
        const res = await apiClient.get<ApiResponse<TeacherStatusReportData>>(endpoint);
        setData(res.data);
      } catch (err) {
        console.error(err);
        setError(t("errorLoading"));
      } finally {
        setLoading(false);
      }
    };
    fetchReport();
  }, [selectedYear, t]);

  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedYear(e.target.value);
  };

  if (loading || isAcademicYearLoading || !academicYears) {
    return (
      <div className="space-y-4 p-8">
        <div className="grid grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <Skeleton key={i} className="h-32 w-full" />
          ))}
        </div>
        <Skeleton className="h-[300px] w-full" />
        <div className="text-muted-foreground">{t("loading")}</div>
      </div>
    );
  }

  if (error) return <div className="p-8 text-red-500">{error}</div>;

  return (
    <div className="container mx-auto py-8 px-4">
      <div className="mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{t("title")}</h1>
          <p className="text-muted-foreground mt-1">
            {t("subtitle")}
          </p>
        </div>
        <div>
          <label htmlFor="academicYear" className="mr-2 font-medium">{t("academicYearLabel")}</label>
          <select
            id="academicYear"
            value={selectedYear}
            onChange={handleYearChange}
            className="border rounded px-2 py-1"
          >
            {academicYears.map((record) => (
              <option key={record.id} value={record.id}>
                {record.yearName}
              </option>
            ))}
          </select>
        </div>
      </div>
      {data && <TeacherStatusReportView data={data} />}
    </div>
  );
}