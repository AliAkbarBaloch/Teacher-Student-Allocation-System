import { 
  PieChart, Pie, Cell, ResponsiveContainer, Tooltip as RechartsTooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';
import { 
  Download, 
  AlertTriangle, 
  CheckCircle2, 
  Clock, 
  Users, 
  School, 
  BookOpen,
  AlertCircle
} from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../../../components/ui/tabs";
import { ScrollArea } from "../../../components/ui/scroll-area";


// --- Types based on your JSON ---
export interface AllocationReportData {
  header: {
    planName: string;
    planVersion: string;
    academicYear: string;
    status: string;
    generatedAt: string;
  };
  budgetSummary: {
    totalBudgetHours: number;
    usedHours: number;
    remainingHours: number;
    elementaryHoursUsed: number;
    middleSchoolHoursUsed: number;
    overBudget: boolean;
  };
  assignments: Array<{
    assignmentId: number;
    teacherName: string;
    schoolName: string;
    schoolZone: string;
    internshipCode: string;
    subjectCode: string;
    studentGroupSize: number;
    assignmentStatus: string;
  }>;
  utilizationAnalysis: {
    unassignedTeachers: TeacherUtilDto[];
    underUtilizedTeachers: TeacherUtilDto[];
    overUtilizedTeachers: TeacherUtilDto[];
    perfectlyUtilizedTeachers: TeacherUtilDto[];
  };
}

interface TeacherUtilDto {
  teacherId: number;
  teacherName: string;
  email: string;
  schoolName: string;
  assignmentCount: number;
  notes: string | null;
}

interface AllocationReportViewProps {
  data: AllocationReportData;
  onExport: () => void;
}

// --- Helper Components ---

const StatusBadge = ({ status }: { status: string }) => {
  const styles = {
    CONFIRMED: "bg-green-100 text-green-800 hover:bg-green-100 border-green-200",
    PLANNED: "bg-blue-100 text-blue-800 hover:bg-blue-100 border-blue-200",
    DRAFT: "bg-gray-100 text-gray-800 hover:bg-gray-100 border-gray-200",
  };
  return (
    <Badge className={styles[status as keyof typeof styles] || "bg-gray-100 text-gray-800"}>
      {status}
    </Badge>
  );
};

export default function AllocationReportView({ data, onExport }: AllocationReportViewProps) {
  const { header, budgetSummary, assignments, utilizationAnalysis } = data;

  // Chart Data Preparation
  const budgetPieData = [
    { name: 'Used', value: budgetSummary.usedHours, color: '#3b82f6' }, // Blue
    { name: 'Remaining', value: budgetSummary.remainingHours, color: '#e5e7eb' }, // Gray
  ];

  const schoolSplitData = [
    { name: 'Elementary', value: budgetSummary.elementaryHoursUsed, fill: '#10b981' }, // Emerald
    { name: 'Middle School', value: budgetSummary.middleSchoolHoursUsed, fill: '#8b5cf6' }, // Violet
  ];

  const utilizationStats = [
    { name: 'Unassigned', value: utilizationAnalysis.unassignedTeachers.length, fill: '#ef4444' },
    { name: 'Under', value: utilizationAnalysis.underUtilizedTeachers.length, fill: '#f59e0b' },
    { name: 'Perfect', value: utilizationAnalysis.perfectlyUtilizedTeachers.length, fill: '#22c55e' },
    { name: 'Over', value: utilizationAnalysis.overUtilizedTeachers.length, fill: '#3b82f6' },
  ];

  return (
    <div className="space-y-6 pb-10">
      
      {/* 1. HEADER SECTION */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <h1 className="text-3xl font-bold tracking-tight">{header.planName}</h1>
            <Badge variant="outline" className="text-base px-3">
              V {header.planVersion}
            </Badge>
            <StatusBadge status={header.status} />
          </div>
          <p className="text-muted-foreground flex items-center gap-2">
            <Clock className="h-4 w-4" />
            Generated: {new Date(header.generatedAt).toLocaleString()} | Academic Year: {header.academicYear}
          </p>
        </div>
        <Button onClick={onExport} className="w-full md:w-auto">
          <Download className="mr-2 h-4 w-4" /> Export Excel
        </Button>
      </div>
      
      {/* 2. CRITICAL ALERTS (If any) */}
      {(utilizationAnalysis.unassignedTeachers.length > 0 || budgetSummary.overBudget) && (
        <Card className="border-destructive/50 bg-destructive/5">
          <CardHeader className="pb-2">
            <CardTitle className="text-destructive flex items-center gap-2 text-lg">
              <AlertTriangle className="h-5 w-5" /> Attention Required
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="list-disc list-inside text-sm space-y-1 text-destructive-foreground">
              {budgetSummary.overBudget && (
                <li>Budget exceeded by {Math.abs(budgetSummary.remainingHours)} hours.</li>
              )}
              {utilizationAnalysis.unassignedTeachers.length > 0 && (
                <li>{utilizationAnalysis.unassignedTeachers.length} teachers are currently unassigned (0 assignments).</li>
              )}
              {utilizationAnalysis.overUtilizedTeachers.length > 0 && (
                <li>{utilizationAnalysis.overUtilizedTeachers.length} teachers are overloaded ({'>'}2 assignments).</li>
              )}
            </ul>
          </CardContent>
        </Card>
      )}

      {/* 3. BUDGET & STATS OVERVIEW */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Budget Circle */}
        <Card className="flex flex-col">
          <CardHeader className="pb-0">
            <CardTitle className="text-lg font-medium">Credit Hour Budget</CardTitle>
            <CardDescription>Total Allocated: {budgetSummary.totalBudgetHours}h</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 min-h-[200px] relative">
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie
                  data={budgetPieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                  startAngle={180}
                  endAngle={0}
                >
                  {budgetPieData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Legend verticalAlign="bottom" height={36}/>
                <RechartsTooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="absolute top-[40%] left-0 right-0 text-center">
              <span className="text-3xl font-bold">{budgetSummary.usedHours}h</span>
              <span className="block text-xs text-muted-foreground">Used</span>
            </div>
          </CardContent>
        </Card>

        {/* School Type Split */}
        <Card>
          <CardHeader className="pb-0">
            <CardTitle className="text-lg font-medium">School Type Distribution</CardTitle>
            <CardDescription>Elementary vs. Middle School hours</CardDescription>
          </CardHeader>
          <CardContent className="min-h-[200px]">
             <ResponsiveContainer width="100%" height={200}>
              <BarChart data={schoolSplitData} layout="vertical" margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" hide />
                <YAxis dataKey="name" type="category" width={100} tick={{fontSize: 12}} />
                <RechartsTooltip cursor={{fill: 'transparent'}} />
                <Bar dataKey="value" barSize={30} radius={[0, 4, 4, 0]}>
                  {schoolSplitData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Teacher Status Summary */}
        <Card>
          <CardHeader className="pb-0">
            <CardTitle className="text-lg font-medium">Teacher Utilization</CardTitle>
            <CardDescription>Efficiency of assignments</CardDescription>
          </CardHeader>
          <CardContent className="min-h-[200px]">
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={utilizationStats} margin={{ top: 20, right: 10, left: 0, bottom: 0 }}>
                 <CartesianGrid strokeDasharray="3 3" vertical={false} />
                 <XAxis dataKey="name" tick={{fontSize: 12}} />
                 <YAxis allowDecimals={false} />
                 <RechartsTooltip />
                 <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                    {utilizationStats.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.fill} />
                    ))}
                 </Bar>
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* 4. DETAILED TABS */}
      <Tabs defaultValue="assignments" className="w-full">
        <TabsList className="grid w-full grid-cols-3 lg:w-[400px]">
          <TabsTrigger value="assignments">All Assignments</TabsTrigger>
          <TabsTrigger value="gaps" className="relative">
             Gaps & Issues
             {utilizationAnalysis.unassignedTeachers.length > 0 && (
                <span className="absolute -top-1 -right-1 h-2 w-2 rounded-full bg-destructive" />
             )}
          </TabsTrigger>
          <TabsTrigger value="perfect">Perfect Matches</TabsTrigger>
        </TabsList>

        {/* Tab 1: All Assignments Table */}
        <TabsContent value="assignments" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Detailed Assignment List</CardTitle>
              <CardDescription>
                Showing all {assignments.length} confirmed allocations for {header.academicYear}.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-[500px] w-full pr-4">
                <div className="space-y-4">
                  {assignments.map((assignment) => (
                    <div key={assignment.assignmentId} className="flex items-center justify-between p-4 border rounded-lg hover:bg-slate-50 transition-colors">
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 w-full">
                        
                        {/* Teacher & School */}
                        <div className="flex items-start gap-3">
                           <div className="mt-1 bg-primary/10 p-2 rounded-full text-primary">
                             <Users className="h-4 w-4" />
                           </div>
                           <div>
                             <p className="font-medium text-sm">{assignment.teacherName}</p>
                             <p className="text-xs text-muted-foreground flex items-center gap-1">
                               <School className="h-3 w-3" /> {assignment.schoolName}
                             </p>
                             <Badge variant="secondary" className="mt-1 text-[10px]">{assignment.schoolZone}</Badge>
                           </div>
                        </div>

                        {/* Assignment Details */}
                        <div className="flex items-start gap-3">
                           <div className="mt-1 bg-orange-100 p-2 rounded-full text-orange-600">
                             <BookOpen className="h-4 w-4" />
                           </div>
                           <div>
                             <p className="font-medium text-sm">{assignment.internshipCode} - {assignment.subjectCode}</p>
                             <p className="text-xs text-muted-foreground">Group Size: {assignment.studentGroupSize}</p>
                           </div>
                        </div>

                        {/* Status */}
                        <div className="flex items-center md:justify-end">
                           <StatusBadge status={assignment.assignmentStatus} />
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Tab 2: Gaps & Issues */}
        <TabsContent value="gaps" className="mt-4">
          <div className="grid gap-6 md:grid-cols-2">
            
            {/* Unassigned List */}
            <Card className="border-l-4 border-l-destructive">
              <CardHeader>
                <CardTitle className="text-destructive flex items-center gap-2">
                  <AlertCircle className="h-5 w-5" /> Unassigned Teachers ({utilizationAnalysis.unassignedTeachers.length})
                </CardTitle>
                <CardDescription>These teachers have 0 assignments but are active.</CardDescription>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-[400px]">
                  {utilizationAnalysis.unassignedTeachers.map((t) => (
                    <div key={t.teacherId} className="mb-4 p-3 bg-destructive/5 rounded-md border border-destructive/10">
                      <div className="font-semibold text-sm">{t.teacherName}</div>
                      <div className="text-xs text-muted-foreground">{t.schoolName}</div>
                      <div className="text-xs text-muted-foreground">{t.email}</div>
                      <div className="mt-2 text-xs font-medium text-destructive">{t.notes}</div>
                    </div>
                  ))}
                  {utilizationAnalysis.unassignedTeachers.length === 0 && (
                    <div className="text-center py-10 text-muted-foreground">
                      <CheckCircle2 className="h-10 w-10 mx-auto text-green-500 mb-2" />
                      No unassigned teachers found.
                    </div>
                  )}
                </ScrollArea>
              </CardContent>
            </Card>

            {/* Under/Over Utilized */}
            <Card className="border-l-4 border-l-yellow-500">
               <CardHeader>
                <CardTitle className="text-yellow-600 flex items-center gap-2">
                  <AlertTriangle className="h-5 w-5" /> Improper Utilization
                </CardTitle>
                <CardDescription>Teachers with 1 assignment (Under) or &gt;2 (Over).</CardDescription>
              </CardHeader>
              <CardContent>
                <ScrollArea className="h-[400px]">
                   <div className="space-y-4">
                      {/* Over Utilized */}
                      {utilizationAnalysis.overUtilizedTeachers.map(t => (
                        <div key={t.teacherId} className="p-3 bg-blue-50 rounded-md border border-blue-100">
                           <div className="flex justify-between">
                              <span className="font-semibold text-sm">{t.teacherName}</span>
                              <Badge variant="destructive">Overloaded ({t.assignmentCount})</Badge>
                           </div>
                           <div className="text-xs text-muted-foreground mt-1">{t.schoolName}</div>
                        </div>
                      ))}

                      {/* Under Utilized */}
                      {utilizationAnalysis.underUtilizedTeachers.map(t => (
                        <div key={t.teacherId} className="p-3 bg-yellow-50 rounded-md border border-yellow-100">
                           <div className="flex justify-between">
                              <span className="font-semibold text-sm">{t.teacherName}</span>
                              <Badge variant="outline" className="border-yellow-500 text-yellow-700">Underutilized ({t.assignmentCount})</Badge>
                           </div>
                           <div className="text-xs text-muted-foreground mt-1">{t.schoolName}</div>
                        </div>
                      ))}
                   </div>
                </ScrollArea>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Tab 3: Perfect Matches */}
        <TabsContent value="perfect" className="mt-4">
           <Card className="border-l-4 border-l-green-500">
              <CardHeader>
                <CardTitle className="text-green-700 flex items-center gap-2">
                  <CheckCircle2 className="h-5 w-5" /> Perfectly Utilized
                </CardTitle>
                <CardDescription>Teachers with exactly 2 assignments (1 Credit Hour).</CardDescription>
              </CardHeader>
              <CardContent>
                 <ScrollArea className="h-[500px]">
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                       {utilizationAnalysis.perfectlyUtilizedTeachers.map(t => (
                          <div key={t.teacherId} className="p-3 border rounded-md hover:bg-green-50/50 transition-colors">
                             <div className="font-medium text-sm">{t.teacherName}</div>
                             <div className="text-xs text-muted-foreground">{t.schoolName}</div>
                             <div className="mt-2">
                                <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">2 Assignments</Badge>
                             </div>
                          </div>
                       ))}
                    </div>
                 </ScrollArea>
              </CardContent>
           </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}