import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"; // Assuming you have shadcn/ui or similar
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Switch } from "@/components/ui/switch";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import useAcademicYears from "@/hooks/entities/useAcademicYears"; // Your existing hook
import { apiClient } from "@/lib/api-client";
import { AlertCircle, Play, Settings2 } from "lucide-react";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";

// --- Types ---

export interface AllocationRunParams {
  isCurrent: boolean;
  planVersion: string;
  prioritizeScarcity: boolean;
  forceUtilizationOfSurplus: boolean;
  allowGroupSizeExpansion: boolean;
  standardAssignmentsPerTeacher: number;
  maxAssignmentsPerTeacher: number;
  maxGroupSizeWednesday: number;
  maxGroupSizeBlock: number;
  weightMainSubject: number;
  weightZonePreference: number;
}

interface AllocationRunResponse {
  message: string;
  success: boolean;
  data: {
    planId: number;
    planName: string;
    planVersion: string;
    academicYear: string;
    status: string;
  };
}

interface AllocationRunModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (newPlanId: number) => void;
}

// --- Default Values ---
const DEFAULT_PARAMS: AllocationRunParams = {
  isCurrent: true,
  planVersion: "1.0-Optimized",
  prioritizeScarcity: true,
  forceUtilizationOfSurplus: true,
  allowGroupSizeExpansion: true,
  standardAssignmentsPerTeacher: 2,
  maxAssignmentsPerTeacher: 3,
  maxGroupSizeWednesday: 4,
  maxGroupSizeBlock: 2,
  weightMainSubject: 10,
  weightZonePreference: 5,
};

export default function AllocationRunModal({ isOpen, onClose, onSuccess }: AllocationRunModalProps) {
  const { t } = useTranslation("allocation");
  const { data: academicYears, isLoading: isYearsLoading } = useAcademicYears();
  
  const [selectedYearId, setSelectedYearId] = useState<string>("");
  const [params, setParams] = useState<AllocationRunParams>(DEFAULT_PARAMS);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Handle Input Changes
  const handleParamChange = (key: keyof AllocationRunParams, value: any) => {
    setParams((prev) => ({ ...prev, [key]: value }));
  };

  const handleNumberChange = (key: keyof AllocationRunParams, value: string) => {
    const num = parseInt(value);
    if (!isNaN(num)) {
      setParams((prev) => ({ ...prev, [key]: num }));
    }
  };

  const handleSubmit = async () => {
    if (!selectedYearId) {
      toast.error(t("Please select an academic year"));
      return;
    }

    setIsSubmitting(true);
    try {
      // API call to run allocation
      const response = await apiClient.post<AllocationRunResponse>(
        `/allocation/run-improved/${selectedYearId}`,
        params
      );

      if (response.success) {
        toast.success(response.success);
        toast.info(`Created Plan: ${response.data.planName} (${response.data.planVersion})`);
        
        // Callback to parent to refresh data
        onSuccess(response.data.planId);
        onClose();
      } else {
        toast.error("Allocation failed without error exception.");
      }
    } catch (error: any) {
      console.error("Allocation Error", error);
      const msg = error?.response?.data?.message || "Failed to generate allocation plan.";
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] flex flex-col p-0 gap-0">
        
        {/* Header */}
        <DialogHeader className="p-6 pb-2 border-b">
          <DialogTitle className="flex items-center gap-2 text-xl">
            <Settings2 className="h-5 w-5 text-primary" />
            {t("Generate Allocation Plan")}
          </DialogTitle>
          <DialogDescription>
            {t("Configure algorithm parameters and select a year to generate a new draft.")}
          </DialogDescription>
        </DialogHeader>

        {/* Scrollable Form Content */}
        <ScrollArea className="flex-1 p-6">
          <div className="space-y-6">
            
            {/* 1. Academic Year Selection */}
            <div className="space-y-2">
              <Label className="text-base font-semibold">{t("Academic Year")}</Label>
              <Select value={selectedYearId} onValueChange={setSelectedYearId}>
                <SelectTrigger>
                  <SelectValue placeholder={isYearsLoading ? t("Loading...") : t("Select Academic Year")} />
                </SelectTrigger>
                <SelectContent>
                  {academicYears?.map((year) => (
                    <SelectItem key={year.id} value={String(year.id)}>
                      {year.yearName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* 2. Configuration Tabs */}
            <Tabs defaultValue="settings" className="w-full">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="settings">{t("General Settings")}</TabsTrigger>
                <TabsTrigger value="constraints">{t("Constraints")}</TabsTrigger>
                <TabsTrigger value="weights">{t("Weights & Strategy")}</TabsTrigger>
              </TabsList>

              {/* General Settings */}
              <TabsContent value="settings" className="space-y-4 border rounded-md p-4 mt-2">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>{t("Plan Version Name")}</Label>
                    <Input 
                      value={params.planVersion} 
                      onChange={(e) => handleParamChange("planVersion", e.target.value)} 
                    />
                  </div>
                  <div className="flex items-center justify-between space-x-2 border p-3 rounded-md">
                    <Label htmlFor="isCurrent" className="cursor-pointer">{t("Set as Active Plan")}</Label>
                    <Switch 
                      id="isCurrent" 
                      checked={params.isCurrent} 
                      onCheckedChange={(c) => handleParamChange("isCurrent", c)} 
                    />
                  </div>
                </div>
              </TabsContent>

              {/* Constraints */}
              <TabsContent value="constraints" className="space-y-4 border rounded-md p-4 mt-2">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label>{t("Std. Assignments / Teacher")}</Label>
                    <Input 
                      type="number" 
                      min={1} 
                      value={params.standardAssignmentsPerTeacher} 
                      onChange={(e) => handleNumberChange("standardAssignmentsPerTeacher", e.target.value)} 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>{t("Max Assignments (Emergency)")}</Label>
                    <Input 
                      type="number" 
                      min={1} 
                      value={params.maxAssignmentsPerTeacher} 
                      onChange={(e) => handleNumberChange("maxAssignmentsPerTeacher", e.target.value)} 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>{t("Max Group Size (Wednesday)")}</Label>
                    <Input 
                      type="number" 
                      min={1} 
                      value={params.maxGroupSizeWednesday} 
                      onChange={(e) => handleNumberChange("maxGroupSizeWednesday", e.target.value)} 
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>{t("Max Group Size (Block)")}</Label>
                    <Input 
                      type="number" 
                      min={1} 
                      value={params.maxGroupSizeBlock} 
                      onChange={(e) => handleNumberChange("maxGroupSizeBlock", e.target.value)} 
                    />
                  </div>
                </div>
              </TabsContent>

              {/* Weights & Strategy */}
              <TabsContent value="weights" className="space-y-4 border rounded-md p-4 mt-2">
                <div className="space-y-4">
                   {/* Switches */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <div className="space-y-0.5">
                        <Label>{t("Prioritize Scarcity")}</Label>
                        <p className="text-xs text-muted-foreground">{t("Fill subjects with few teachers first")}</p>
                      </div>
                      <Switch 
                        checked={params.prioritizeScarcity} 
                        onCheckedChange={(c: boolean) => handleParamChange("prioritizeScarcity", c)} 
                      />
                    </div>
                    <div className="flex items-center justify-between">
                       <div className="space-y-0.5">
                        <Label>{t("Force Surplus Usage")}</Label>
                        <p className="text-xs text-muted-foreground">{t("Assign unused teachers to PDP/Support")}</p>
                      </div>
                      <Switch 
                        checked={params.forceUtilizationOfSurplus} 
                        onCheckedChange={(c: boolean) => handleParamChange("forceUtilizationOfSurplus", c)} 
                      />
                    </div>
                     <div className="flex items-center justify-between">
                       <div className="space-y-0.5">
                        <Label>{t("Allow Size Expansion")}</Label>
                        <p className="text-xs text-muted-foreground">{t("Slightly exceed group limits if needed")}</p>
                      </div>
                      <Switch 
                        checked={params.allowGroupSizeExpansion} 
                        onCheckedChange={(c: boolean) => handleParamChange("allowGroupSizeExpansion", c)} 
                      />
                    </div>
                  </div>
                  
                  <div className="h-px bg-border my-2" />

                  {/* Weight Inputs */}
                  <div className="grid grid-cols-2 gap-4">
                     <div className="space-y-2">
                      <Label>{t("Weight: Main Subject Match")}</Label>
                      <Input 
                        type="number" 
                        value={params.weightMainSubject} 
                        onChange={(e) => handleNumberChange("weightMainSubject", e.target.value)} 
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>{t("Weight: Zone Preference")}</Label>
                      <Input 
                        type="number" 
                        value={params.weightZonePreference} 
                        onChange={(e) => handleNumberChange("weightZonePreference", e.target.value)} 
                      />
                    </div>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
            
            {/* Warning if no year selected */}
            {!selectedYearId && (
              <div className="flex items-center gap-2 p-3 text-sm text-amber-800 bg-amber-50 rounded-md border border-amber-200">
                <AlertCircle className="h-4 w-4" />
                {t("You must select an academic year to proceed.")}
              </div>
            )}

          </div>
        </ScrollArea>

        {/* Footer actions */}
        <DialogFooter className="p-6 pt-2 border-t mt-auto">
          <Button variant="outline" onClick={onClose} disabled={isSubmitting}>
            {t("Cancel")}
          </Button>
          <Button 
            onClick={handleSubmit} 
            disabled={!selectedYearId || isSubmitting}
            className="min-w-[140px]"
          >
            {isSubmitting ? (
              <>
                <span className="animate-spin mr-2">⏳</span> {t("Processing...")}
              </>
            ) : (
              <>
                <Play className="mr-2 h-4 w-4" /> {t("Run Allocation")}
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}