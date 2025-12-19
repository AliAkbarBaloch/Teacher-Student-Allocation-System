// react
import { useState, useEffect, useCallback, useMemo } from "react";
// router
import { useParams, useNavigate } from "react-router-dom";
// translation
import { useTranslation } from "react-i18next";
import i18n from "@/lib/i18n";
// notifications
import { toast } from "sonner";
// icons
import { Loader2, CheckCircle2, AlertCircle } from "lucide-react";
// components
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { MultiSelect } from "@/components/ui/multi-select";
import { TeacherFormSubmissionService } from "@/features/teacher-submissions/services/teacherFormSubmissionService";
import { SchoolService } from "@/features/schools/services/schoolService";
import { SubjectService } from "@/features/subjects/services/subjectService";
import { InternshipTypeService } from "@/features/internship-types/services/internshipTypeService";
import type { FormLinkResponse } from "@/features/teacher-submissions/types/teacherFormSubmission.types";
import type { School } from "@/features/schools/types/school.types";
import type { SchoolType } from "@/features/schools/types/school.types";
import type { Subject } from "@/features/subjects/types/subject.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";

const SCHOOL_TYPE_OPTIONS: SchoolType[] = ["PRIMARY", "MIDDLE"];

interface FormData {
  schoolType: SchoolType | "";
  schoolId: number | null;
  notes: string;
  subjectIds: number[];
  internshipTypeIds: number[];
}

export default function TeacherFormPage() {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const { t } = useTranslation("teacherSubmissions");
  
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [formDetails, setFormDetails] = useState<FormLinkResponse | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [schools, setSchools] = useState<School[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [internshipTypes, setInternshipTypes] = useState<InternshipType[]>([]);
  const [loadingSchools, setLoadingSchools] = useState(false);
  const [loadingSubjects, setLoadingSubjects] = useState(false);
  const [loadingInternshipTypes, setLoadingInternshipTypes] = useState(false);

  const [formData, setFormData] = useState<FormData>({
    schoolType: "",
    schoolId: null,
    notes: "",
    subjectIds: [],
    internshipTypeIds: [],
  });

  // Set language to German when component mounts
  useEffect(() => {
    i18n.changeLanguage("de");
  }, []);

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
        
        // Load schools, subjects, and internship types
        setLoadingSchools(true);
        setLoadingSubjects(true);
        setLoadingInternshipTypes(true);
        
        const [schoolsRes, subjectsRes, internshipTypesRes] = await Promise.all([
          SchoolService.getPaginated({ isActive: true, page: 1, pageSize: 1000, sortBy: "schoolName", sortOrder: "asc" }),
          SubjectService.getAll(),
          InternshipTypeService.getAll(),
        ]);
        
        setSchools(schoolsRes.items || []);
        setSubjects(subjectsRes || []);
        setInternshipTypes(internshipTypesRes || []);
      } catch (err) {
        const message = err instanceof Error ? err.message : t("publicForm.errors.loadFailed");
        setError(message);
        toast.error(message);
      } finally {
        setLoading(false);
        setLoadingSchools(false);
        setLoadingSubjects(false);
        setLoadingInternshipTypes(false);
      }
    };

    loadData();
  }, [token, t]);

  // Reset school selection when school type changes
  useEffect(() => {
    if (formData.schoolType) {
      setFormData((prev) => ({ ...prev, schoolId: null }));
    }
  }, [formData.schoolType]);

  // Memoize filtered schools to avoid recalculating on every render
  const filteredSchools = useMemo(() => {
    if (!formData.schoolType) return [];
    return schools.filter((school) => school.schoolType === formData.schoolType);
  }, [schools, formData.schoolType]);

  // Memoize options arrays for MultiSelect components
  const subjectOptions = useMemo(
    () =>
      subjects.map((s) => ({
        label: `${s.subjectCode} - ${s.subjectTitle}`,
        value: s.id,
      })),
    [subjects]
  );

  const internshipTypeOptions = useMemo(
    () =>
      internshipTypes.map((type) => ({
        label: `${type.internshipCode} - ${type.fullName}`,
        value: type.id,
      })),
    [internshipTypes]
  );

  // Form field handlers with useCallback for performance
  // Using functional updates to avoid dependency on formData
  const handleSchoolTypeChange = useCallback((value: string) => {
    setFormData((prev) => ({
      ...prev,
      schoolType: value as SchoolType | "",
      schoolId: null,
    }));
  }, []);

  const handleSchoolChange = useCallback((value: string) => {
    setFormData((prev) => ({ ...prev, schoolId: Number(value) }));
  }, []);

  const handleSubjectsChange = useCallback((selected: (string | number)[]) => {
    setFormData((prev) => ({
      ...prev,
      subjectIds: selected as number[],
    }));
  }, []);

  const handleInternshipTypesChange = useCallback((selected: (string | number)[]) => {
    setFormData((prev) => ({
      ...prev,
      internshipTypeIds: selected as number[],
    }));
  }, []);

  const handleNotesChange = useCallback((e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setFormData((prev) => ({ ...prev, notes: e.target.value }));
  }, []);

  // Validation helper
  const validateForm = useCallback((): string | null => {
    if (!formData.schoolType) {
      return t("publicForm.validation.schoolTypeRequired");
    }
    if (!formData.schoolId) {
      return t("publicForm.validation.schoolRequired");
    }
    if (formData.subjectIds.length === 0) {
      return t("publicForm.validation.subjectsRequired");
    }
    if (formData.internshipTypeIds.length === 0) {
      return t("publicForm.validation.internshipTypesRequired");
    }
    return null;
  }, [formData, t]);

  const handleSubmit = useCallback(
    async (e: React.FormEvent) => {
      e.preventDefault();

      const validationError = validateForm();
      if (validationError) {
        toast.error(validationError);
        return;
      }

      if (!token) {
        toast.error(t("publicForm.errors.invalidToken"));
        return;
      }

      setSubmitting(true);
      try {
        await TeacherFormSubmissionService.submitFormByToken(token, {
          schoolId: formData.schoolId!,
          notes: formData.notes,
          subjectIds: formData.subjectIds,
          internshipTypeIds: formData.internshipTypeIds,
        });
        setSubmitted(true);
        toast.success(t("publicForm.success.submitted"));
      } catch (err) {
        const message =
          err instanceof Error
            ? err.message
            : t("publicForm.errors.submitFailed");
        toast.error(message);
      } finally {
        setSubmitting(false);
      }
    },
    [formData, token, t, validateForm]
  );

  // Memoize form validation to avoid recalculating on every render
  // Must be before any early returns to follow Rules of Hooks
  const isFormValid = useMemo(
    () =>
      formData.schoolType !== "" &&
      formData.schoolId !== null &&
      formData.subjectIds.length > 0 &&
      formData.internshipTypeIds.length > 0,
    [
      formData.schoolType,
      formData.schoolId,
      formData.subjectIds.length,
      formData.internshipTypeIds.length,
    ]
  );

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

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>{t("publicForm.teacher")}</Label>
                  <div className="p-3 bg-muted rounded-md min-h-[60px] flex flex-col justify-center mt-2">
                    <p className="font-medium">{formDetails.teacherName}</p>
                    <p className="text-sm text-muted-foreground">
                      {formDetails.teacherEmail}
                    </p>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label>{t("publicForm.academicYear")}</Label>
                  <div className="p-3 bg-muted rounded-md min-h-[68px] flex items-center mt-2">
                    <p className="font-medium">{formDetails.yearName}</p>
                  </div>
                </div>
              </div>
              {/* Two-column layout for dropdowns */}
              <div className="grid gap-4 md:grid-cols-2">
                {/* School Type */}
                <div className="space-y-2">
                  <Label htmlFor="schoolType">
                    {t("publicForm.schoolType")} *
                  </Label>
                  <Select
                    value={formData.schoolType}
                    onValueChange={handleSchoolTypeChange}
                  >
                    <SelectTrigger id="schoolType" className="h-9 w-full mt-2">
                      <SelectValue
                        placeholder={t("publicForm.selectSchoolType")}
                      />
                    </SelectTrigger>
                    <SelectContent>
                      {SCHOOL_TYPE_OPTIONS.map((option) => (
                        <SelectItem key={option} value={option}>
                          {t(`publicForm.schoolTypeOptions.${option}`)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {/* School Name */}
                <div className="space-y-2">
                  <Label htmlFor="school">{t("publicForm.school")} *</Label>
                  <Select
                    value={formData.schoolId?.toString() || ""}
                    onValueChange={handleSchoolChange}
                    disabled={loadingSchools || !formData.schoolType}
                  >
                    <SelectTrigger id="school" className="h-9 w-full mt-2">
                      <SelectValue placeholder={t("publicForm.selectSchool")} />
                    </SelectTrigger>
                    <SelectContent>
                      {filteredSchools.map((school) => (
                        <SelectItem
                          key={school.id}
                          value={school.id.toString()}
                        >
                          {school.schoolName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {!formData.schoolType && (
                    <p className="text-sm text-muted-foreground">
                      {t("publicForm.selectSchoolTypeFirst")}
                    </p>
                  )}
                </div>
              </div>
              {/* Two-column layout for multi-selects */}
              <div className="grid gap-4 md:grid-cols-2">
                {/* Subjects */}
                <div className="space-y-2">
                  <Label className="mb-2">{t("publicForm.subjects")} *</Label>
                  <MultiSelect
                    options={subjectOptions}
                    selected={formData.subjectIds}
                    onChange={handleSubjectsChange}
                    placeholder={t("publicForm.selectSubjects")}
                    disabled={loadingSubjects}
                    className={`mt-2 ${
                      formData.subjectIds.length === 0 ? "h-9" : ""
                    }`}
                  />
                  <p className="text-sm text-muted-foreground">
                    {t("publicForm.subjectsHint")}
                  </p>
                </div>

                {/* Internship Types */}
                <div className="space-y-2">
                  <Label>{t("publicForm.internshipTypes")} *</Label>
                  <MultiSelect
                    options={internshipTypeOptions}
                    selected={formData.internshipTypeIds}
                    onChange={handleInternshipTypesChange}
                    placeholder={t("publicForm.selectInternshipTypes")}
                    disabled={loadingInternshipTypes}
                    className={`mt-2 ${
                      formData.internshipTypeIds.length === 0 ? "h-9" : ""
                    }`}
                  />
                  <p className="text-sm text-muted-foreground">
                    {t("publicForm.internshipTypesHint")}
                  </p>
                </div>
              </div>
              {/* Notes */}
              <div className="space-y-2">
                <Label htmlFor="notes">{t("publicForm.notes")}</Label>
                <Textarea
                  id="notes"
                  value={formData.notes}
                  onChange={handleNotesChange}
                  placeholder={t("publicForm.notesPlaceholder")}
                  rows={4}
                  maxLength={5000}
                  className="mt-2"
                />
                <p className="text-sm text-muted-foreground">
                  {t("publicForm.notesHint")}
                </p>
              </div>
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
