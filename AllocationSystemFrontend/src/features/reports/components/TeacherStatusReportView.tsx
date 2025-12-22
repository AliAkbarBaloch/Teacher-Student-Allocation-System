import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import {
    AlertCircle,
    Briefcase,
    CheckCircle2,
    Clock,
    Filter,
    GraduationCap,
    School,
    Search,
    Users,
    XCircle
} from 'lucide-react';
import { useMemo, useState } from 'react';
import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Tooltip as RechartsTooltip, ResponsiveContainer,
    XAxis, YAxis
} from 'recharts';
import { useTranslation } from "react-i18next";

// --- Types based on your Java DTOs ---
export interface TeacherStatusReportData {
  metrics: {
    totalTeachers: number;
    activeCount: number;
    onLeaveCount: number;
    inactiveCount: number;
    partTimeCount: number;
    fullTimeCount: number;
    subjectCoverageCounts: Record<string, number>;
  };
  profiles: TeacherProfileDto[];
}

export interface TeacherProfileDto {
  teacherId: number;
  fullName: string;
  email: string;
  schoolName: string;
  schoolType: string;
  employmentStatus: "ACTIVE" | "INACTIVE_THIS_YEAR" | "ON_LEAVE" | "ARCHIVED";
  isPartTime: boolean;
  workingHours: number | null;
  qualifiedSubjects: string[];
  availabilityStatusForYear: string;
  availabilityNotes: string;
}

interface TeacherStatusReportViewProps {
  data: TeacherStatusReportData;
}

// --- Helper Components ---

const StatusBadge = ({ status }: { status: string }) => {
  const { t } = useTranslation("reportTeachers");
  const styles = {
    ACTIVE: "bg-emerald-100 text-emerald-800 border-emerald-200 hover:bg-emerald-100",
    ON_LEAVE: "bg-amber-100 text-amber-800 border-amber-200 hover:bg-amber-100",
    INACTIVE_THIS_YEAR: "bg-slate-100 text-slate-800 border-slate-200 hover:bg-slate-100",
    ARCHIVED: "bg-destructive/10 text-destructive border-destructive/20 hover:bg-destructive/10",
  };
  return (
    <Badge variant="outline" className={styles[status as keyof typeof styles] || styles.ACTIVE}>
      {t(`status.${status}`, status)}
    </Badge>
  );
};

export default function TeacherStatusReportView({ data }: TeacherStatusReportViewProps) {
  const { t } = useTranslation("reportTeachers");
  const { metrics, profiles } = data;
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");

  // Prepare Chart Data
  const subjectData = useMemo(() => {
    return Object.entries(metrics.subjectCoverageCounts)
      .map(([name, count]) => ({ name, count }))
      .sort((a, b) => b.count - a.count); // Sort highest to lowest
  }, [metrics.subjectCoverageCounts]);

  // Filter Logic
  const filteredProfiles = useMemo(() => {
    return profiles.filter(teacher => {
      const matchesSearch = 
        teacher.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        teacher.schoolName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        teacher.qualifiedSubjects.some(s => s.toLowerCase().includes(searchTerm.toLowerCase()));
      
      const matchesStatus = statusFilter === "ALL" || teacher.employmentStatus === statusFilter;
      
      return matchesSearch && matchesStatus;
    });
  }, [profiles, searchTerm, statusFilter]);

  return (
    <div className="space-y-8 pb-10">
      
      {/* 1. TOP METRICS ROW */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t("metrics.totalTeachers")}</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.totalTeachers}</div>
            <p className="text-xs text-muted-foreground">{t("metrics.registered")}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t("metrics.activePool")}</CardTitle>
            <CheckCircle2 className="h-4 w-4 text-emerald-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.activeCount}</div>
            <p className="text-xs text-muted-foreground">
              {t("metrics.availabilityRate", { rate: ((metrics.activeCount / metrics.totalTeachers) * 100).toFixed(0) })}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t("metrics.workforceType")}</CardTitle>
            <Briefcase className="h-4 w-4 text-blue-600" />
          </CardHeader>
          <CardContent>
            <div className="flex justify-between items-end">
              <div>
                <div className="text-2xl font-bold">{metrics.fullTimeCount}</div>
                <p className="text-xs text-muted-foreground">{t("metrics.fullTime")}</p>
              </div>
              <div className="text-right">
                <div className="text-2xl font-bold text-muted-foreground">{metrics.partTimeCount}</div>
                <p className="text-xs text-muted-foreground">{t("metrics.partTime")}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">{t("metrics.unavailable")}</CardTitle>
            <AlertCircle className="h-4 w-4 text-amber-600" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.onLeaveCount + metrics.inactiveCount}</div>
            <p className="text-xs text-muted-foreground">
              {t("metrics.onLeaveInactive", { onLeave: metrics.onLeaveCount, inactive: metrics.inactiveCount })}
            </p>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* 2. SUBJECT DISTRIBUTION CHART (Left 2/3) */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>{t("subjectCoverage.title")}</CardTitle>
            <CardDescription>{t("subjectCoverage.description")}</CardDescription>
          </CardHeader>
          <CardContent className="pl-0">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={subjectData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{fontSize: 12}} interval={0} angle={-15} textAnchor="end" height={60} />
                <YAxis allowDecimals={false} />
                <RechartsTooltip 
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                  cursor={{fill: 'transparent'}}
                />
                <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} barSize={40}>
                   {subjectData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.count < 5 ? '#ef4444' : '#3b82f6'} />
                   ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
            <div className="flex items-center gap-2 justify-center mt-2 text-xs text-muted-foreground">
               <span className="flex items-center gap-1"><div className="w-3 h-3 bg-blue-500 rounded-sm"></div> {t("subjectCoverage.healthy")}</span>
               <span className="flex items-center gap-1"><div className="w-3 h-3 bg-red-500 rounded-sm"></div> {t("subjectCoverage.critical")}</span>
            </div>
          </CardContent>
        </Card>

        {/* 3. QUICK FILTERS & SUMMARY (Right 1/3) */}
        <Card>
          <CardHeader>
            <CardTitle>{t("filters.title")}</CardTitle>
            <CardDescription>{t("filters.description")}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("filters.searchLabel")}</label>
              <div className="relative">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input 
                  placeholder={t("filters.searchPlaceholder")}
                  className="pl-8"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
            
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("filters.statusLabel")}</label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue placeholder={t("filters.allStatuses")} />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">{t("filters.allStatuses")}</SelectItem>
                  <SelectItem value="ACTIVE">{t("filters.activeOnly")}</SelectItem>
                  <SelectItem value="ON_LEAVE">{t("filters.onLeave")}</SelectItem>
                  <SelectItem value="INACTIVE_THIS_YEAR">{t("filters.inactiveThisYear")}</SelectItem>
                </SelectContent>
              </Select>
            </div>
            
            <Separator className="my-4"/>
            
            <div className="bg-muted/50 p-4 rounded-lg">
              <h4 className="font-semibold mb-2 text-sm flex items-center gap-2">
                <Filter className="h-4 w-4" /> {t("filters.currentViewStats")}
              </h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">{t("filters.showing")}</span>
                  <span className="font-medium">{t("filters.teachers", { count: filteredProfiles.length })}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 4. DETAILED TEACHER LIST */}
      <Card>
        <CardHeader>
          <CardTitle>{t("directory.title")}</CardTitle>
          <CardDescription>{t("directory.description")}</CardDescription>
        </CardHeader>
        <CardContent>
          <ScrollArea className="h-[600px] pr-4">
            <div className="space-y-3">
              {filteredProfiles.length > 0 ? filteredProfiles.map((teacher) => (
                <div 
                  key={teacher.teacherId} 
                  className="group flex flex-col md:flex-row md:items-center justify-between p-4 rounded-lg border hover:bg-slate-50 hover:border-slate-300 transition-all"
                >
                  {/* Left: Avatar/Name/School */}
                  <div className="flex items-start gap-4 mb-4 md:mb-0 md:w-1/3">
                    <div className={`mt-1 p-2 rounded-full ${teacher.isPartTime ? 'bg-orange-100 text-orange-600' : 'bg-blue-100 text-blue-600'}`}>
                      {teacher.isPartTime ? <Clock className="h-5 w-5" /> : <Briefcase className="h-5 w-5" />}
                    </div>
                    <div>
                      <div className="font-semibold text-base flex items-center gap-2">
                        {teacher.fullName}
                      </div>
                      <div className="text-sm text-muted-foreground flex items-center gap-1 mt-0.5">
                        <School className="h-3.5 w-3.5" />
                        {teacher.schoolName}
                        <Badge variant="secondary" className="text-[10px] h-5 px-1.5 ml-1">
                          {teacher.schoolType}
                        </Badge>
                      </div>
                      <div className="text-xs text-muted-foreground mt-1">
                        {teacher.email}
                      </div>
                    </div>
                  </div>

                  {/* Middle: Subjects */}
                  <div className="mb-4 md:mb-0 md:w-1/3">
                    <div className="flex flex-wrap gap-1.5">
                      {teacher.qualifiedSubjects.length > 0 ? (
                        <>
                          {teacher.qualifiedSubjects.slice(0, 3).map(subject => (
                            <Badge key={subject} variant="outline" className="bg-white">
                              {subject}
                            </Badge>
                          ))}
                          {teacher.qualifiedSubjects.length > 3 && (
                            <Badge variant="secondary" className="text-xs">
                              +{teacher.qualifiedSubjects.length - 3} more
                            </Badge>
                          )}
                        </>
                      ) : (
                        <span className="text-sm text-muted-foreground italic">{t("directory.noSubjects")}</span>
                      )}
                    </div>
                    {teacher.workingHours && (
                       <p className="text-xs text-muted-foreground mt-2">
                         {t("directory.contract", { hours: teacher.workingHours })}
                       </p>
                    )}
                  </div>

                  {/* Right: Status & Availability */}
                  <div className="flex flex-col items-end md:w-1/4 gap-2">
                    <StatusBadge status={teacher.employmentStatus} />
                    
                    {/* Availability Context for the specific year */}
                    {teacher.availabilityStatusForYear !== "NOT_SET" && (
                      <div className={`text-xs px-2 py-1 rounded-md mt-1 flex items-center gap-1.5
                        ${teacher.availabilityStatusForYear === 'AVAILABLE' ? 'bg-green-50 text-green-700' : 
                          teacher.availabilityStatusForYear === 'PREFERRED' ? 'bg-blue-50 text-blue-700' : 
                          'bg-red-50 text-red-700'}`
                      }>
                        {teacher.availabilityStatusForYear === 'AVAILABLE' && <CheckCircle2 className="h-3 w-3"/>}
                        {teacher.availabilityStatusForYear === 'NOT_AVAILABLE' && <XCircle className="h-3 w-3"/>}
                        
                        <span className="font-medium">
                           {teacher.availabilityStatusForYear === 'PREFERRED' ? t("directory.preferred") : 
                            teacher.availabilityStatusForYear === 'AVAILABLE' ? t("directory.available") : t("directory.unavailable")}
                        </span>
                      </div>
                    )}
                    {teacher.availabilityNotes && (
                      <span className="text-[10px] text-muted-foreground text-right max-w-[150px] truncate">
                        "{teacher.availabilityNotes}"
                      </span>
                    )}
                  </div>
                </div>
              )) : (
                <div className="text-center py-12 text-muted-foreground bg-muted/20 rounded-lg border border-dashed">
                  <GraduationCap className="h-10 w-10 mx-auto mb-3 opacity-20" />
                  <p>{t("directory.noTeachers")}</p>
                  <button 
                    onClick={() => { setSearchTerm(""); setStatusFilter("ALL"); }}
                    className="text-sm text-primary hover:underline mt-2"
                  >
                    {t("directory.clearFilters")}
                  </button>
                </div>
              )}
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  );
}