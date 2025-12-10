import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { 
  GraduationCap, 
  School, 
  FileText, 
  ClipboardList,
  RefreshCw,
  AlertCircle
} from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useDashboard } from "@/features/dashboard/hooks/useDashboard";
import { StatCard } from "@/features/dashboard/components/StatCard";
import { AllocationUtilization } from "@/features/dashboard/components/AllocationUtilization";
import { AllocationPlansStatus } from "@/features/dashboard/components/AllocationPlansStatus";
import { ROUTES } from "@/config/routes";

export default function HomePage() {
  const { t } = useTranslation("common");
  const navigate = useNavigate();
  const { stats, loading, error, refresh } = useDashboard();

  if (error) {
    return (
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">{t("dashboard.title")}</h1>
            <p className="text-muted-foreground mt-1">{t("dashboard.subtitle")}</p>
          </div>
        </div>
        <Card className="border-destructive">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              {t("dashboard.error.title")}
            </CardTitle>
            <CardDescription>{error}</CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={refresh} variant="outline">
              <RefreshCw className="h-4 w-4 mr-2" />
              {t("dashboard.error.retry")}
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{t("dashboard.title")}</h1>
          <p className="text-muted-foreground mt-1">{t("dashboard.subtitle")}</p>
        </div>
        <Button onClick={refresh} variant="outline" size="sm" disabled={loading}>
          <RefreshCw className={`h-4 w-4 mr-2 ${loading ? "animate-spin" : ""}`} />
          {t("dashboard.refresh")}
        </Button>
      </div>

      {/* Statistics Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {loading ? (
          <>
            {[...Array(4)].map((_, i) => (
              <Card key={i}>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <Skeleton className="h-4 w-24" />
                  <Skeleton className="h-4 w-4 rounded" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-8 w-16 mb-2" />
                  <Skeleton className="h-3 w-32" />
                </CardContent>
              </Card>
            ))}
          </>
        ) : stats ? (
          <>
            <StatCard
              title={t("dashboard.stats.teachers.title")}
              value={stats.teachers.total}
              description={t("dashboard.stats.teachers.description")}
              icon={GraduationCap}
              iconColor="text-green-600"
              onClick={() => navigate(ROUTES.baseData.teachers)}
            />
            <StatCard
              title={t("dashboard.stats.schools.title")}
              value={stats.schools.total}
              description={t("dashboard.stats.schools.description")}
              icon={School}
              iconColor="text-purple-600"
              onClick={() => navigate(ROUTES.baseData.schools)}
            />
            <StatCard
              title={t("dashboard.stats.allocationPlans.title")}
              value={stats.allocationPlans.total}
              description={t("dashboard.stats.allocationPlans.description")}
              icon={FileText}
              iconColor="text-orange-600"
              onClick={() => navigate(ROUTES.allocationPlanning.allocationPlans)}
            />
            <StatCard
              title={t("dashboard.stats.pendingSubmissions.title")}
              value={stats.pendingSubmissions.total}
              description={t("dashboard.stats.pendingSubmissions.description")}
              icon={ClipboardList}
              iconColor="text-red-600"
              onClick={() => navigate(ROUTES.teacherManagement.teacherSubmissions)}
            />
          </>
        ) : null}
      </div>

      {/* Allocation Utilization & Plans Status */}
      <div className="grid gap-4 md:grid-cols-2">
        {stats && (
          <>
            <AllocationUtilization utilization={stats.utilization} loading={loading} />
            <AllocationPlansStatus plansByStatus={stats.plansByStatus} loading={loading} />
          </>
        )}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>{t("dashboard.quickActions.title")}</CardTitle>
          <CardDescription>{t("dashboard.quickActions.description")}</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-2 md:grid-cols-2 lg:grid-cols-4">
            <Button
              variant="outline"
              className="w-full justify-start"
              onClick={() => navigate(ROUTES.allocationPlanning.allocationPlans)}
            >
              <FileText className="h-4 w-4 mr-2" />
              {t("dashboard.quickActions.createAllocationPlan")}
            </Button>
            <Button
              variant="outline"
              className="w-full justify-start"
              onClick={() => navigate(ROUTES.baseData.teachers)}
            >
              <GraduationCap className="h-4 w-4 mr-2" />
              {t("dashboard.quickActions.manageTeachers")}
            </Button>
            <Button
              variant="outline"
              className="w-full justify-start"
              onClick={() => navigate(ROUTES.teacherManagement.teacherSubmissions)}
            >
              <ClipboardList className="h-4 w-4 mr-2" />
              {t("dashboard.quickActions.reviewSubmissions")}
            </Button>
            <Button
              variant="outline"
              className="w-full justify-start"
              onClick={() => navigate(ROUTES.allocationPlanning.creditHourTracking)}
            >
              <FileText className="h-4 w-4 mr-2" />
              {t("dashboard.quickActions.viewCreditHours")}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
