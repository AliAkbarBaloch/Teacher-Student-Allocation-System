import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  Filter,
  Users,
  Wallet
} from 'lucide-react';
import { useMemo, useState } from 'react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ComposedChart,
  Legend,
  Pie,
  PieChart,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  XAxis, YAxis
} from 'recharts';
import { useTranslation } from "react-i18next";

export interface AllocationHealthReportDto {
  planName: string;
  academicYear: string;
  status: string;
  totalBudget: BudgetMetric;
  elementaryBudget: BudgetMetric;
  middleSchoolBudget: BudgetMetric;
  totalStudentCount: number;
  totalRequiredTeachers: number;
  totalAssignedTeachers: number;
  fulfillmentPercentage: number;
  isBudgetCompliant: boolean;
  complianceWarning: string;
}

export interface BudgetMetric {
  allocated: number;
  used: number;
  remaining: number;
}

export interface SubjectBottleneckDto {
  subjectName: string;
  schoolType: string;
  requiredTeacherCount: number;
  availableTeacherCount: number;
  actuallyAssignedCount: number;
  gap: number;
  status: "CRITICAL_SHORTAGE" | "SHORTAGE" | "BALANCED" | "SURPLUS";
}

export interface TeacherUtilizationReportDto {
  teacherId: number;
  teacherName: string;
  schoolName: string;
  assignmentsInCurrentPlan: number;
  currentCreditBalance: number;
  utilizationStatus: "UNUSED" | "UNDER_UTILIZED" | "OPTIMAL" | "OVER_UTILIZED";
  isUnused: boolean;
}

interface PlanAnalyticsViewProps {
  healthData: AllocationHealthReportDto;
  bottleneckData: SubjectBottleneckDto[];
  utilizationData: TeacherUtilizationReportDto[];
}

export default function PlanAnalyticsView({ healthData, bottleneckData, utilizationData }: PlanAnalyticsViewProps) {
  const { t } = useTranslation("reportPlanAnalytics");

  if (!healthData) return <div className="p-8 text-red-500">{t("noAnalyticsData")}</div>;

  return (
    <div className="space-y-8 pb-10">
      {/* Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="rounded-lg border bg-white p-6 flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <Wallet className="h-5 w-5 text-muted-foreground" />
            <span className="font-medium text-sm">{t("metrics.totalBudget")}</span>
          </div>
          <div className="text-2xl font-bold">{healthData.totalBudget.allocated}</div>
          <div className="text-xs text-muted-foreground">
            {t("metrics.usedRemaining", { used: healthData.totalBudget.used, remaining: healthData.totalBudget.remaining })}
          </div>
        </div>
        <div className="rounded-lg border bg-white p-6 flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <Users className="h-5 w-5 text-muted-foreground" />
            <span className="font-medium text-sm">{t("metrics.totalStudents")}</span>
          </div>
          <div className="text-2xl font-bold">{healthData.totalStudentCount}</div>
          <div className="text-xs text-muted-foreground">
            {t("metrics.teachersAssigned", { count: healthData.totalAssignedTeachers })}
          </div>
        </div>
        <div className="rounded-lg border bg-white p-6 flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="h-5 w-5 text-emerald-600" />
            <span className="font-medium text-sm">{t("metrics.fulfillment")}</span>
          </div>
          <div className="text-2xl font-bold">{healthData.fulfillmentPercentage.toFixed(1)}%</div>
          <div className="text-xs text-muted-foreground">
            {t("metrics.filled", { assigned: healthData.totalAssignedTeachers, required: healthData.totalRequiredTeachers })}
          </div>
        </div>
        <div className="rounded-lg border bg-white p-6 flex flex-col gap-2">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-destructive" />
            <span className="font-medium text-sm">{t("metrics.compliance")}</span>
          </div>
          <div className="text-2xl font-bold">
            {healthData.isBudgetCompliant ? t("metrics.compliant") : t("metrics.warning")}
          </div>
          <div className="text-xs text-muted-foreground">
            {healthData.isBudgetCompliant ? t("metrics.withinBudget") : healthData.complianceWarning}
          </div>
        </div>
      </div>

      {/* Main Content Tabs */}
      <Tabs defaultValue="health" className="w-full">
        <TabsList className="grid w-full grid-cols-3 lg:w-[600px]">
          <TabsTrigger value="health" className="flex items-center gap-2 px-4 py-2">
            <Wallet className="w-4 h-4" />
            {t("tabs.health")}
          </TabsTrigger>
          <TabsTrigger value="bottlenecks" className="flex items-center gap-2 px-4 py-2">
            <AlertTriangle className="w-4 h-4" />
            {t("tabs.bottlenecks")}
          </TabsTrigger>
          <TabsTrigger value="utilization" className="flex items-center gap-2 px-4 py-2">
            <Users className="w-4 h-4" />
            {t("tabs.utilization")}
          </TabsTrigger>
        </TabsList>

        <div className="mt-6">
          <TabsContent value="health">
            <HealthView data={healthData} />
          </TabsContent>
          <TabsContent value="bottlenecks">
            <BottleneckView data={bottleneckData || []} />
          </TabsContent>
          <TabsContent value="utilization">
            <UtilizationView data={utilizationData || []} />
          </TabsContent>
        </div>
      </Tabs>
    </div>
  );
}

// --- Sub-View: Health & Budget ---
function HealthView({ data }: { data: AllocationHealthReportDto }) {
  const { t } = useTranslation("reportPlanAnalytics");
  const budgetData = [
    { name: t("health.used"), value: data.totalBudget.used, fill: '#3b82f6' },
    { name: t("health.remaining"), value: data.totalBudget.remaining, fill: '#e2e8f0' },
  ];

  const splitData = [
    { name: t("health.elementary"), allocated: data.elementaryBudget.allocated, used: data.elementaryBudget.used },
    { name: t("health.middleSchool"), allocated: data.middleSchoolBudget.allocated, used: data.middleSchoolBudget.used },
  ];

  return (
    <div className="space-y-8">
      {!data.isBudgetCompliant && (
        <div className="rounded-lg border border-destructive/50 bg-destructive/5 p-4 flex items-center gap-4 text-destructive">
          <AlertTriangle className="h-6 w-6" />
          <div>
            <h3 className="font-semibold">{t("health.complianceWarningTitle")}</h3>
            <p className="text-sm opacity-90">{data.complianceWarning}</p>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* 1. Total Budget Gauge */}
        <div className="rounded-lg border bg-white p-4 flex flex-col items-center">
          <div className="font-semibold mb-2">{t("health.totalBudget")}</div>
          <ResponsiveContainer width="100%" height={180}>
            <PieChart>
              <Pie
                data={budgetData}
                cx="50%"
                cy="70%"
                startAngle={180}
                endAngle={0}
                innerRadius={60}
                outerRadius={80}
                dataKey="value"
              >
                {budgetData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.fill} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
          <div className="text-center mt-2">
            <span className="text-2xl font-bold">{((data.totalBudget.used / data.totalBudget.allocated) * 100).toFixed(1)}%</span>
            <p className="text-xs text-muted-foreground">{t("health.utilized")}</p>
          </div>
        </div>

        {/* 2. Budget Split Chart */}
        <div className="rounded-lg border bg-white p-4 md:col-span-2">
          <div className="font-semibold mb-2">{t("health.budgetSplitAnalysis")}</div>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={splitData} layout="vertical" margin={{ top: 5, right: 30, left: 40, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" horizontal={false} />
              <XAxis type="number" />
              <YAxis dataKey="name" type="category" width={100} tick={{ fontSize: 12 }} />
              <RechartsTooltip cursor={{ fill: 'transparent' }} />
              <Legend />
              <Bar dataKey="allocated" name={t("health.limit")} fill="#94a3b8" barSize={20} radius={[0, 4, 4, 0]} />
              <Bar dataKey="used" name={t("health.used")} fill="#3b82f6" barSize={20} radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* 3. Fulfillment Metrics */}
        <div className="rounded-lg border bg-white p-4 flex flex-col gap-2">
          <div className="font-semibold mb-2">{t("health.demandFulfillment")}</div>
          <div className="text-2xl font-bold">{data.fulfillmentPercentage.toFixed(1)}%</div>
          <p className="text-xs text-muted-foreground mb-2">{t("health.ofRequiredFilled")}</p>
          <div className="space-y-1 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t("health.teachersAssigned")}</span>
              <span className="font-medium">{data.totalAssignedTeachers}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t("health.teachersRequired")}</span>
              <span className="font-medium">{data.totalRequiredTeachers}</span>
            </div>
            <Separator />
            <div className="flex justify-between">
              <span className="text-muted-foreground">{t("health.studentsCovered")}</span>
              <span className="font-medium">{data.totalStudentCount}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// --- Sub-View: Bottlenecks ---
function BottleneckView({ data }: { data: SubjectBottleneckDto[] }) {
  const { t } = useTranslation("reportPlanAnalytics");
  const chartData = useMemo(() => {
    return [...data]
      .filter(item => item.status !== "SURPLUS")
      .sort((a, b) => a.gap - b.gap)
      .slice(0, 10);
  }, [data]);

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chart */}
        <div className="rounded-lg border bg-white p-4 lg:col-span-2">
          <div className="font-semibold mb-2">{t("bottlenecks.top10SupplyGaps")}</div>
          <ResponsiveContainer width="100%" height={300}>
            <ComposedChart data={chartData} layout="vertical" margin={{ top: 20, right: 20, bottom: 20, left: 40 }}>
              <CartesianGrid stroke="#f5f5f5" horizontal={false} />
              <XAxis type="number" />
              <YAxis dataKey="subjectName" type="category" width={100} tick={{ fontSize: 11 }} />
              <RechartsTooltip />
              <Legend />
              <Bar dataKey="availableTeacherCount" name={t("bottlenecks.availableSupply")} barSize={20} fill="#94a3b8" radius={[0, 4, 4, 0]} />
              <Bar dataKey="requiredTeacherCount" name={t("bottlenecks.requiredDemand")} barSize={20} fill="#ef4444" radius={[0, 4, 4, 0]} />
            </ComposedChart>
          </ResponsiveContainer>
        </div>

        {/* Critical List */}
        <div className="rounded-lg border bg-white p-4 lg:col-span-1">
          <div className="font-semibold mb-2 text-destructive flex items-center gap-2">
            <AlertCircle className="h-5 w-5" /> {t("bottlenecks.criticalShortages")}
          </div>
          <ScrollArea className="h-[300px] pr-4">
            <div className="space-y-4">
              {data.filter(i => i.status === "CRITICAL_SHORTAGE" || i.status === "SHORTAGE").map((item, idx) => (
                <div key={idx} className="flex flex-col p-3 border rounded-lg bg-red-50/50 border-red-100">
                  <div className="flex justify-between items-start">
                    <span className="font-semibold text-sm">{item.subjectName}</span>
                    <Badge variant="destructive" className="text-[10px]">{t("bottlenecks.gap", { gap: item.gap })}</Badge>
                  </div>
                  <div className="text-xs text-muted-foreground mt-1">
                    {t("bottlenecks.needsHas", { schoolType: item.schoolType, required: item.requiredTeacherCount, available: item.availableTeacherCount })}
                  </div>
                  <div className="mt-2 text-xs font-medium text-red-600">
                    {t("bottlenecks.suggestion")}
                  </div>
                </div>
              ))}
              {data.filter(i => i.status === "CRITICAL_SHORTAGE" || i.status === "SHORTAGE").length === 0 && (
                <div className="text-center py-8 text-muted-foreground">
                  <CheckCircle2 className="h-8 w-8 mx-auto text-green-500 mb-2" />
                  {t("bottlenecks.noCriticalShortages")}
                </div>
              )}
            </div>
          </ScrollArea>
        </div>
      </div>
    </div>
  );
}

// --- Sub-View: Utilization ---
function UtilizationView({ data }: { data: TeacherUtilizationReportDto[] }) {
  const { t } = useTranslation("reportPlanAnalytics");
  const [filter, setFilter] = useState("ALL");

  const filteredData = useMemo(() => {
    if (filter === "ALL") return data;
    return data.filter(tch => tch.utilizationStatus === filter);
  }, [data, filter]);

  const stats = useMemo(() => ({
    unused: data.filter(tch => tch.utilizationStatus === "UNUSED").length,
    over: data.filter(tch => tch.utilizationStatus === "OVER_UTILIZED").length,
    under: data.filter(tch => tch.utilizationStatus === "UNDER_UTILIZED").length,
  }), [data]);

  return (
    <div className="space-y-8">
      {/* Metrics Row */}
      <div className="grid grid-cols-3 gap-4">
        <div className="p-4 border rounded-lg bg-red-50 border-red-100 text-center">
          <div className="text-2xl font-bold text-red-600">{stats.unused}</div>
          <div className="text-xs text-red-800 font-medium">{t("utilization.unusedTeachers")}</div>
        </div>
        <div className="p-4 border rounded-lg bg-yellow-50 border-yellow-100 text-center">
          <div className="text-2xl font-bold text-yellow-600">{stats.under}</div>
          <div className="text-xs text-yellow-800 font-medium">{t("utilization.underUtilized")}</div>
        </div>
        <div className="p-4 border rounded-lg bg-blue-50 border-blue-100 text-center">
          <div className="text-2xl font-bold text-blue-600">{stats.over}</div>
          <div className="text-xs text-blue-800 font-medium">{t("utilization.overUtilized")}</div>
        </div>
      </div>

      <div className="rounded-lg border bg-white p-4">
        <div className="flex flex-row items-center justify-between pb-2">
          <div>
            <div className="font-semibold">{t("utilization.detailsTitle")}</div>
            <div className="text-xs text-muted-foreground">{t("utilization.detailsSubtitle")}</div>
          </div>
          <Select value={filter} onValueChange={setFilter}>
            <SelectTrigger className="w-[180px]">
              <div className="flex items-center gap-2">
                <Filter className="h-4 w-4" />
                <SelectValue placeholder={t("utilization.filterStatus")} />
              </div>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">{t("utilization.filter.all")}</SelectItem>
              <SelectItem value="UNUSED">{t("utilization.filter.unused")}</SelectItem>
              <SelectItem value="UNDER_UTILIZED">{t("utilization.filter.under")}</SelectItem>
              <SelectItem value="OPTIMAL">{t("utilization.filter.optimal")}</SelectItem>
              <SelectItem value="OVER_UTILIZED">{t("utilization.filter.over")}</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <ScrollArea className="h-[400px]">
          <div className="space-y-2">
            {filteredData.map(teacher => (
              <div key={teacher.teacherId} className="flex items-center justify-between p-3 border rounded-md hover:bg-slate-50">
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-10 rounded-full ${
                    teacher.utilizationStatus === 'UNUSED' ? 'bg-red-500' :
                    teacher.utilizationStatus === 'OPTIMAL' ? 'bg-green-500' :
                    teacher.utilizationStatus === 'UNDER_UTILIZED' ? 'bg-yellow-500' : 'bg-blue-500'
                  }`} />
                  <div>
                    <div className="font-medium text-sm">{teacher.teacherName}</div>
                    <div className="text-xs text-muted-foreground flex items-center gap-1">
                      <Users className="h-3 w-3" /> {teacher.schoolName}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-6">
                  <div className="text-right">
                    <div className="text-xs text-muted-foreground">{t("utilization.assignments")}</div>
                    <div className="font-bold text-sm">{teacher.assignmentsInCurrentPlan}</div>
                  </div>
                  <div className="text-right w-24">
                    <div className="text-xs text-muted-foreground">{t("utilization.creditBalance")}</div>
                    <div className={`font-bold text-sm ${teacher.currentCreditBalance < 0 ? 'text-red-600' : 'text-green-600'}`}>
                      {teacher.currentCreditBalance > 0 ? '+' : ''}{teacher.currentCreditBalance} {t("utilization.hrs")}
                    </div>
                  </div>
                  <Badge variant="outline" className={`w-24 justify-center ${
                    teacher.utilizationStatus === 'UNUSED' ? 'border-red-500 text-red-700 bg-red-50' :
                    teacher.utilizationStatus === 'OPTIMAL' ? 'border-green-500 text-green-700 bg-green-50' : ''
                  }`}>
                    {t(`utilization.status.${teacher.utilizationStatus}`)}
                  </Badge>
                </div>
              </div>
            ))}
            {filteredData.length === 0 && (
              <div className="text-center py-8 text-muted-foreground">{t("utilization.noTeachersMatch")}</div>
            )}
          </div>
        </ScrollArea>
      </div>
    </div>
  );
}