// react
import { useState, useEffect } from "react";
// translations
import { useTranslation } from "react-i18next";
// notifications
import { toast } from "sonner";
// icons
import { Copy, Check, Loader2, Link2, AlertCircle } from "lucide-react";
// dialog
import {
  Dialog,
  DialogBody,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
// components
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

// hooks
import { apiClient } from "@/lib/api-client";
import { TeacherFormSubmissionService } from "../services/teacherFormSubmissionService";
import { TeacherService } from "@/features/teachers/services/teacherService";
// types
import type { Teacher } from "@/features/teachers/types/teacher.types";
import type { FormLinkResponse } from "../types/teacherFormSubmission.types";

interface AcademicYear {
  id: number;
  yearName: string;
  startDate?: string;
  endDate?: string;
  isLocked?: boolean;
}

interface GenerateFormLinkDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  teacherId?: number;
  yearId?: number;
  onLinkGenerated?: () => void;
}

export function GenerateFormLinkDialog({
  open,
  onOpenChange,
  teacherId: initialTeacherId,
  yearId: initialYearId,
  onLinkGenerated,
}: GenerateFormLinkDialogProps) {
  const { t } = useTranslation("teacherSubmissions");
  const [teacherId, setTeacherId] = useState<number | undefined>(initialTeacherId);
  const [yearId, setYearId] = useState<number | undefined>(initialYearId);
  const [teachers, setTeachers] = useState<Teacher[]>([]);
  const [academicYears, setAcademicYears] = useState<AcademicYear[]>([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [formLink, setFormLink] = useState<FormLinkResponse | null>(null);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load teachers and academic years
  useEffect(() => {
    if (!open) return;

    const loadData = async () => {
      setLoading(true);
      try {
        const [teachersRes, yearsRes] = await Promise.all([
          TeacherService.list({ page: 1, pageSize: 1000, sortBy: "lastName", sortOrder: "asc" }),
          apiClient.get<{ success: boolean; data: AcademicYear[] }>("/academic-years"),
        ]);
        setTeachers(teachersRes.items || []);
        setAcademicYears(yearsRes.data || []);
      } catch (error) {
        console.error("Failed to load data:", error);
        toast.error(t("errors.loadFailed"));
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [open, t]);

  // Reset form when dialog closes
  useEffect(() => {
    if (!open) {
      setFormLink(null);
      setCopied(false);
      setError(null);
      if (!initialTeacherId) setTeacherId(undefined);
      if (!initialYearId) setYearId(undefined);
    }
  }, [open, initialTeacherId, initialYearId]);

  const getErrorMessage = (error: unknown): string => {
    if (error instanceof Error) {
      const errorMessage = error.message;
      const errorMessageLower = errorMessage.toLowerCase();
      
      // Check for specific error messages from backend
      if (errorMessageLower.includes("already exists") || 
          errorMessageLower.includes("submission already exists") ||
          errorMessageLower.includes("form submission already exists")) {
        return t("formLink.errors.alreadyExists");
      }
      if (errorMessageLower.includes("locked academic year") || 
          errorMessageLower.includes("locked") ||
          errorMessageLower.includes("cannot generate form link for locked")) {
        return t("formLink.errors.yearLocked");
      }
      if (errorMessageLower.includes("teacher not found")) {
        return t("formLink.errors.teacherNotFound");
      }
      if (errorMessageLower.includes("academic year not found") || 
          errorMessageLower.includes("year not found")) {
        return t("formLink.errors.yearNotFound");
      }
      if (errorMessageLower.includes("network") || 
          errorMessageLower.includes("fetch") ||
          errorMessageLower.includes("failed to fetch")) {
        return t("formLink.errors.networkError");
      }
      
      // If we have a meaningful error message from the backend, use it
      // Otherwise fall back to generic error
      if (errorMessage && errorMessage.trim() !== "" && errorMessage !== "An error occurred") {
        return errorMessage;
      }
      
      return t("formLink.errors.generateFailed");
    }
    return t("formLink.errors.unknownError");
  };

  const handleGenerate = async () => {
    if (!teacherId || !yearId) {
      const errorMsg = t("formLink.errors.missingFields");
      setError(errorMsg);
      toast.error(errorMsg);
      return;
    }

    setError(null);
    setGenerating(true);
    try {
      const link = await TeacherFormSubmissionService.generateFormLink({
        teacherId,
        yearId,
      });
      setFormLink(link);
      setError(null);
      toast.success(t("formLink.success.generated"));
      
      // Refresh the table immediately after generating link
      // The backend creates the record synchronously, so we can refresh right away
      if (onLinkGenerated) {
        // Call immediately - backend transaction commits synchronously
        onLinkGenerated();
      }
    } catch (err) {
      const errorMessage = getErrorMessage(err);
      setError(errorMessage);
      toast.error(errorMessage);
      // Only log to console in development mode
      if (import.meta.env.DEV) {
        console.error("Failed to generate form link:", err);
      }
    } finally {
      setGenerating(false);
    }
  };

  const handleCopy = async () => {
    if (!formLink?.formUrl) return;

    try {
      await navigator.clipboard.writeText(formLink.formUrl);
      setCopied(true);
      toast.success(t("formLink.success.copied"));
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      toast.error(t("formLink.errors.copyFailed", { error: (error as Error).message }));
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>{t("formLink.title")}</DialogTitle>
          <DialogDescription>{t("formLink.description")}</DialogDescription>
        </DialogHeader>
        <DialogBody>
          {!formLink ? (
            <div className="space-y-4">
              {error && (
                <div className="rounded-lg border border-destructive/50 bg-destructive/10 p-4">
                  <div className="flex items-start gap-2">
                    <AlertCircle className="h-5 w-5 text-destructive mt-0.5 shrink-0" />
                    <div className="flex-1">
                      <p className="text-sm font-medium text-destructive">{t("formLink.errors.title")}</p>
                      <p className="text-sm text-destructive/90 mt-1">{error}</p>
                    </div>
                  </div>
                </div>
              )}
              <div className="space-y-2">
                <Label htmlFor="teacher">{t("formLink.teacher")}</Label>
                <Select
                  value={teacherId?.toString() || ""}
                  onValueChange={(value) => {
                    setTeacherId(value ? Number(value) : undefined);
                    setError(null);
                  }}
                  disabled={loading || !!initialTeacherId}
                >
                  <SelectTrigger id="teacher">
                    <SelectValue placeholder={t("formLink.selectTeacher")} />
                  </SelectTrigger>
                  <SelectContent>
                    {teachers.map((teacher) => (
                      <SelectItem key={teacher.id} value={teacher.id.toString()}>
                        {teacher.firstName} {teacher.lastName} ({teacher.email})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="year">{t("formLink.academicYear")}</Label>
                <Select
                  value={yearId?.toString() || ""}
                  onValueChange={(value) => {
                    setYearId(value ? Number(value) : undefined);
                    setError(null);
                  }}
                  disabled={loading || !!initialYearId}
                >
                  <SelectTrigger id="year">
                    <SelectValue placeholder={t("formLink.selectYear")} />
                  </SelectTrigger>
                  <SelectContent>
                    {academicYears.map((year) => (
                      <SelectItem key={year.id} value={year.id.toString()}>
                        {year.yearName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="rounded-lg border bg-muted/50 p-4 space-y-3">
                <div>
                  <Label className="text-sm font-medium">{t("formLink.teacher")}</Label>
                  <p className="text-sm text-muted-foreground">
                    {formLink.teacherName} ({formLink.teacherEmail})
                  </p>
                </div>
                <div>
                  <Label className="text-sm font-medium">{t("formLink.academicYear")}</Label>
                  <p className="text-sm text-muted-foreground">{formLink.yearName}</p>
                </div>
                <div>
                  <Label className="text-sm font-medium">{t("formLink.formUrl")}</Label>
                  <div className="flex items-center gap-2 mt-1">
                    <Input
                      value={formLink.formUrl}
                      readOnly
                      className="font-mono text-xs"
                    />
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={handleCopy}
                      className="shrink-0"
                    >
                      {copied ? (
                        <Check className="h-4 w-4 text-green-600" />
                      ) : (
                        <Copy className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                </div>
              </div>

              <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 text-sm text-blue-900 dark:border-blue-400/30 dark:bg-blue-500/10 dark:text-blue-100">
                <div className="flex items-start gap-2">
                  <Link2 className="h-4 w-4 mt-0.5" />
                  <div>
                    <p className="font-medium">{t("formLink.info.title")}</p>
                    <p className="mt-1">{t("formLink.info.description")}</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </DialogBody>
        <DialogFooter className="p-4">
          {!formLink ? (
            <>
              <Button variant="outline" onClick={() => onOpenChange(false)}>
                {t("formLink.cancel")}
              </Button>
              <Button onClick={handleGenerate} disabled={!teacherId || !yearId || generating}>
                {generating ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    {t("formLink.generating")}
                  </>
                ) : (
                  t("formLink.generate")
                )}
              </Button>
            </>
          ) : (
            <Button onClick={() => onOpenChange(false)}>
              {t("formLink.close")}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

