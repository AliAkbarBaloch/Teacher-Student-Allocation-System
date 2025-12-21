import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
    Bus,
    CheckCircle2,
    Filter,
    MapPin,
    School,
    Search,
    Users
} from 'lucide-react';
import { useMemo, useState } from 'react';
import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    Pie,
    PieChart,
    Tooltip as RechartsTooltip,
    ResponsiveContainer,
    XAxis, YAxis
} from 'recharts';

// --- Types ---
export interface SchoolStatusReportData {
  metrics: {
    totalSchools: number;
    activeSchools: number;
    inactiveSchools: number;
    schoolsByType: Record<string, number>;
    schoolsByZone: Record<string, number>;
    schoolsByAccessibility: Record<string, number>;
  };
  profiles: SchoolProfileDto[];
}

export interface SchoolProfileDto {
  schoolId: number;
  schoolName: string;
  schoolType: string;
  zoneNumber: number;
  transportAccessibility: string | null;
  isActive: boolean;
  totalTeachers: number;
  activeTeachers: number;
}

interface SchoolStatusReportViewProps {
  data: SchoolStatusReportData;
}

// --- Colors & Helpers ---
const COLORS = ['#0ea5e9', '#22c55e', '#eab308', '#f97316', '#ef4444', '#8b5cf6'];

export default function SchoolStatusReportView({ data }: SchoolStatusReportViewProps) {
  const { metrics, profiles } = data;
  const [searchTerm, setSearchTerm] = useState("");
  const [zoneFilter, setZoneFilter] = useState<string>("ALL");

  // Chart Data Preparation
  const typeData = useMemo(() => 
    Object.entries(metrics.schoolsByType).map(([name, value]) => ({ name, value })), 
  [metrics.schoolsByType]);

  const zoneData = useMemo(() => 
    Object.entries(metrics.schoolsByZone)
      .map(([name, value]) => ({ name: `Zone ${name}`, value }))
      .sort((a, b) => a.name.localeCompare(b.name)), 
  [metrics.schoolsByZone]);

  const accessibilityData = useMemo(() => 
    Object.entries(metrics.schoolsByAccessibility)
    .map(([name, value]) => ({ name: name || "None", value })), 
  [metrics.schoolsByAccessibility]);

  // Filtering
  const filteredProfiles = useMemo(() => {
    return profiles.filter(school => {
      const matchesSearch = school.schoolName.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesZone = zoneFilter === "ALL" || school.zoneNumber.toString() === zoneFilter;
      return matchesSearch && matchesZone;
    });
  }, [profiles, searchTerm, zoneFilter]);

  return (
    <div className="space-y-8 pb-10">
      
      {/* 1. METRIC CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="bg-primary/5 border-primary/20">
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-primary">Total Schools</CardTitle>
            <School className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{metrics.totalSchools}</div>
            <p className="text-xs text-muted-foreground mt-1">
              {metrics.activeSchools} Active / {metrics.inactiveSchools} Inactive
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Zone Distribution</CardTitle>
            <MapPin className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{Object.keys(metrics.schoolsByZone).length}</div>
            <p className="text-xs text-muted-foreground mt-1">Active geographic zones</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Transport Access</CardTitle>
            <Bus className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{metrics.schoolsByAccessibility['4a'] || 0}</div>
            <p className="text-xs text-muted-foreground mt-1">Schools within 30 min (Type 4a)</p>
          </CardContent>
        </Card>
      </div>

      {/* 2. CHARTS ROW */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* School Types Pie */}
        <Card className="col-span-1">
          <CardHeader>
            <CardTitle className="text-sm">School Types</CardTitle>
          </CardHeader>
          <CardContent className="h-[250px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={typeData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {typeData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <RechartsTooltip />
                <Legend verticalAlign="bottom" height={36} iconType="circle" wrapperStyle={{fontSize: '12px'}}/>
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Zones Bar */}
        <Card className="col-span-1">
          <CardHeader>
            <CardTitle className="text-sm">Zone Distribution</CardTitle>
          </CardHeader>
          <CardContent className="h-[250px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={zoneData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="name" tick={{fontSize: 10}} />
                <YAxis allowDecimals={false} />
                <RechartsTooltip cursor={{fill: 'transparent'}} />
                <Bar dataKey="value" fill="#8884d8" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Accessibility Bar */}
        <Card className="col-span-1">
          <CardHeader>
            <CardTitle className="text-sm">Transport Accessibility</CardTitle>
          </CardHeader>
          <CardContent className="h-[250px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={accessibilityData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                <XAxis type="number" allowDecimals={false} />
                <YAxis dataKey="name" type="category" width={80} tick={{fontSize: 10}} />
                <RechartsTooltip cursor={{fill: 'transparent'}} />
                <Bar dataKey="value" fill="#22c55e" radius={[0, 4, 4, 0]} barSize={20} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* 3. DETAILED LIST WITH FILTERS */}
      <div className="grid grid-cols-1 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle>School Directory</CardTitle>
              <CardDescription>Teacher capacity and location details</CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <div className="relative w-40 md:w-60">
                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input 
                  placeholder="Search schools..." 
                  className="pl-8"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <Select value={zoneFilter} onValueChange={setZoneFilter}>
                <SelectTrigger className="w-[130px]">
                  <div className="flex items-center gap-2">
                    <Filter className="h-4 w-4" />
                    <SelectValue placeholder="Zone" />
                  </div>
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Zones</SelectItem>
                  {[...Array(6)].map((_, i) => (
                    <SelectItem key={i+1} value={(i+1).toString()}>Zone {i+1}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-[500px]">
              <div className="space-y-2">
                {filteredProfiles.map((school) => (
                  <div 
                    key={school.schoolId} 
                    className="flex flex-col md:flex-row md:items-center justify-between p-4 rounded-lg border hover:bg-slate-50 transition-colors"
                  >
                    {/* Left: Basic Info */}
                    <div className="flex items-start gap-3 mb-3 md:mb-0 md:w-1/3">
                      <div className={`p-2 rounded-full mt-1 ${school.isActive ? 'bg-blue-50 text-blue-600' : 'bg-slate-100 text-slate-500'}`}>
                        <School className="h-5 w-5" />
                      </div>
                      <div>
                        <div className="font-medium flex items-center gap-2">
                          {school.schoolName}
                          {!school.isActive && <Badge variant="secondary" className="text-[10px]">Inactive</Badge>}
                        </div>
                        <div className="text-xs text-muted-foreground flex items-center gap-2 mt-1">
                          <Badge variant="outline" className="text-[10px] font-normal">{school.schoolType}</Badge>
                        </div>
                      </div>
                    </div>

                    {/* Middle: Location Info */}
                    <div className="flex items-center gap-6 mb-3 md:mb-0 md:w-1/3">
                      <div className="flex flex-col">
                        <span className="text-[10px] uppercase text-muted-foreground font-semibold">Zone</span>
                        <span className="text-sm font-medium flex items-center gap-1">
                           <MapPin className="h-3 w-3 text-muted-foreground" /> {school.zoneNumber}
                        </span>
                      </div>
                      <div className="flex flex-col">
                         <span className="text-[10px] uppercase text-muted-foreground font-semibold">Access</span>
                         <span className="text-sm font-medium flex items-center gap-1">
                            <Bus className="h-3 w-3 text-muted-foreground" /> 
                            {school.transportAccessibility || "N/A"}
                         </span>
                      </div>
                    </div>

                    {/* Right: Teacher Capacity */}
                    <div className="flex items-center justify-end md:w-1/3 gap-4">
                      <div className="text-right">
                         <span className="text-[10px] uppercase text-muted-foreground font-semibold block">Teachers</span>
                         <div className="flex items-center gap-2 justify-end">
                            <Users className="h-4 w-4 text-muted-foreground" />
                            <span className="font-bold text-lg">{school.activeTeachers}</span>
                            <span className="text-xs text-muted-foreground">/ {school.totalTeachers} active</span>
                         </div>
                      </div>
                      <div className="h-8 w-1 bg-slate-200 mx-2 hidden md:block"></div>
                      <div>
                         {school.activeTeachers > 0 ? (
                           <CheckCircle2 className="h-5 w-5 text-emerald-500" />
                         ) : (
                           <div className="h-5 w-5 rounded-full border-2 border-slate-300"></div>
                         )}
                      </div>
                    </div>
                  </div>
                ))}
                
                {filteredProfiles.length === 0 && (
                  <div className="text-center py-10 text-muted-foreground">
                    No schools match your search filters.
                  </div>
                )}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}