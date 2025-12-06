// react
import { useState, useEffect } from "react";
// router
import { useParams, useNavigate } from "react-router-dom";
// translation
import { useTranslation } from "react-i18next";
// notifications
import { toast } from "sonner";
// icons
import { Loader2, CheckCircle2, AlertCircle } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { TeacherFormSubmissionService } from "@/features/teacher-form-submissions/services/teacherFormSubmissionService";
import { SchoolService } from "@/features/schools/services/schoolService";
import { SubjectService } from "@/features/subjects/services/subjectService";
import { EMPLOYMENT_STATUS_OPTIONS } from "@/lib/constants/teachers";
import type { FormLinkResponse } from "@/features/teacher-form-submissions/types/teacherFormSubmission.types";
import type { School } from "@/features/schools/types/school.types";
import type { Subject } from "@/features/subjects/types/subject.types";
import type { EmploymentStatus } from "@/features/teachers/types/teacher.types";
import { TeacherInfoSection } from "./components/TeacherInfoSection";
import { SchoolSelectionSection } from "./components/SchoolSelectionSection";
import { SubjectSelectionSection } from "./components/SubjectSelectionSection";
import { AvailabilitySection } from "./components/AvailabilitySection";
import { SemesterAvailabilitySection } from "./components/SemesterAvailabilitySection";
import { NotesSection } from "./components/NotesSection";

interface FormData {
  schoolId: number | null;
  employmentStatus: EmploymentStatus | "";
  notes: string;
  subjectIds: number[];
  semesterAvailability: string[];
  availabilityOptions: string[];
}

export default function TeacherFormPage() {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const { t } = useTranslation("teacherFormSubmissions");
  
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [formDetails, setFormDetails] = useState<FormLinkResponse | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [schools, setSchools] = useState<School[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loadingSchools, setLoadingSchools] = useState(false);
  const [loadingSubjects, setLoadingSubjects] = useState(false);

  const [formData, setFormData] = useState<FormData>({
    schoolId: null,
    employmentStatus: "",
    notes: "",
    subjectIds: [],
    semesterAvailability: [],
    availabilityOptions: [],
  });

  useEffect(() => {
    if (!token) {
      setError(t("publicForm.errors.invalidToken"));
      setLoading(false);
      return;
    }

    const loadData = async () => {
      try {
        const details = await TeacherFormSubmissionService.getFormDetailsByToken(token);
        setFormDetails(details);
        
        // Load schools and subjects
        setLoadingSchools(true);
        setLoadingSubjects(true);
        
        const [schoolsRes, subjectsRes] = await Promise.all([
          SchoolService.list({ isActive: true, page: 1, pageSize: 1000, sortBy: "schoolName", sortOrder: "asc" }),
          SubjectService.getAll(),
        ]);
        
        setSchools(schoolsRes.items || []);
        setSubjects(subjectsRes || []);
      } catch (err) {
        const message = err instanceof Error ? err.message : t("publicForm.errors.loadFailed");
        setError(message);
        toast.error(message);
      } finally {
        setLoading(false);
        setLoadingSchools(false);
        setLoadingSubjects(false);
      }
    };

    loadData();
  }, [token, t]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validation
    if (!formData.schoolId) {
      toast.error(t("publicForm.validation.schoolRequired"));
      return;
    }
    
    if (!formData.employmentStatus) {
      toast.error(t("publicForm.validation.employmentStatusRequired"));
      return;
    }
    
    if (formData.subjectIds.length === 0) {
      toast.error(t("publicForm.validation.subjectsRequired"));
      return;
    }
    
    if (formData.availabilityOptions.length < 2) {
      toast.error(t("publicForm.validation.availabilityOptionsRequired"));
      return;
    }
    
    if (formData.semesterAvailability.length === 0) {
      toast.error(t("publicForm.validation.semesterAvailabilityRequired"));
      return;
    }

    setSubmitting(true);
    try {
      await TeacherFormSubmissionService.submitFormByToken(token!, {
        schoolId: formData.schoolId,
        employmentStatus: formData.employmentStatus,
        notes: formData.notes,
        subjectIds: formData.subjectIds,
        internshipTypePreference: "",
        internshipCombinations: [],
        semesterAvailability: formData.semesterAvailability,
        availabilityOptions: formData.availabilityOptions,
      });
      setSubmitted(true);
      toast.success(t("publicForm.success.submitted"));
    } catch (err) {
      const message = err instanceof Error ? err.message : t("publicForm.errors.submitFailed");
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-muted/30">
        <div className="text-center space-y-4">
          <Loader2 className="h-8 w-8 animate-spin mx-auto text-primary" />
          <p className="text-muted-foreground">{t("publicForm.loading")}</p>
        </div>
      </div>
    );
  }

  if (error || !formDetails) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
        <Card className="w-full max-w-md">
          <CardHeader>
            <div className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              <CardTitle>{t("publicForm.errors.title")}</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground mb-4">
              {error || t("publicForm.errors.notFound")}
            </p>
            <Button onClick={() => navigate("/")} variant="outline" className="w-full">
              {t("publicForm.backToHome")}
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (submitted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
        <Card className="w-full max-w-md">
          <CardHeader>
            <div className="flex items-center gap-2 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
              <CardTitle>{t("publicForm.success.title")}</CardTitle>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-muted-foreground">
              {t("publicForm.success.message", {
                teacherName: formDetails.teacherName,
                yearName: formDetails.yearName,
              })}
            </p>
            <Button onClick={() => navigate("/")} className="w-full">
              {t("publicForm.backToHome")}
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const isFormValid = 
    formData.schoolId !== null &&
    formData.employmentStatus !== "" &&
    formData.subjectIds.length > 0 &&
    formData.availabilityOptions.length >= 2 &&
    formData.semesterAvailability.length > 0;

  return (
    <div className="min-h-screen bg-muted/30 p-4">
      <div className="max-w-4xl mx-auto py-8">
        <Card>
          <CardHeader>
            <CardTitle>{t("publicForm.title")}</CardTitle>
            <CardDescription>
              {t("publicForm.description", {
                teacherName: formDetails.teacherName,
                yearName: formDetails.yearName,
              })}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <TeacherInfoSection formDetails={formDetails} t={t} />

              <SchoolSelectionSection
                schoolId={formData.schoolId}
                schools={schools}
                loading={loadingSchools}
                onChange={(schoolId) => setFormData({ ...formData, schoolId })}
                t={t}
              />

              {/* Employment Status */}
              <div className="space-y-2">
                <Label htmlFor="employmentStatus">{t("publicForm.employmentStatus")} *</Label>
                <Select
                  value={formData.employmentStatus}
                  onValueChange={(value) => setFormData({ ...formData, employmentStatus: value as EmploymentStatus })}
                >
                  <SelectTrigger id="employmentStatus">
                    <SelectValue placeholder={t("publicForm.selectEmploymentStatus")} />
                  </SelectTrigger>
                  <SelectContent>
                    {EMPLOYMENT_STATUS_OPTIONS.map((status) => (
                      <SelectItem key={status} value={status}>
                        {t(`publicForm.employmentStatusOptions.${status}`)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <SubjectSelectionSection
                subjectIds={formData.subjectIds}
                subjects={subjects}
                loading={loadingSubjects}
                onChange={(subjectIds) => setFormData({ ...formData, subjectIds })}
                t={t}
              />

              <AvailabilitySection
                availabilityOptions={formData.availabilityOptions}
                onChange={(options) => setFormData({ ...formData, availabilityOptions: options })}
                t={t}
              />

              <SemesterAvailabilitySection
                semesterAvailability={formData.semesterAvailability}
                onChange={(options) => setFormData({ ...formData, semesterAvailability: options })}
                t={t}
              />

              <NotesSection
                notes={formData.notes}
                onChange={(notes) => setFormData({ ...formData, notes })}
                t={t}
              />

              <div className="flex gap-4 pt-4">
                <Button
                  type="submit"
                  disabled={submitting || !isFormValid}
                  className="flex-1"
                >
                  {submitting ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      {t("publicForm.submitting")}
                    </>
                  ) : (
                    t("publicForm.submit")
                  )}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
