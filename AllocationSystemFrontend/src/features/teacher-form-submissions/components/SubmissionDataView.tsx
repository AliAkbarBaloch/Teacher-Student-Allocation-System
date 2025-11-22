// react
import { useState, useEffect } from "react";
// translations
import { useTranslation } from "react-i18next";
// router
import { Link } from "react-router-dom";
// icons
import { Loader2, CheckCircle2, XCircle } from "lucide-react";
// components
import { Badge } from "@/components/ui/badge";
// config
import { ROUTES } from "@/config/routes";
// hooks
import { SchoolService } from "@/features/schools/services/schoolService";
import { SubjectService } from "@/features/subjects/services/subjectService";
// types
import type { TeacherFormSubmission } from "../types/teacherFormSubmission.types";
import type { School } from "@/features/schools/types/school.types";
import type { Subject } from "@/features/subjects/types/subject.types";

interface SubmissionDataViewProps {
  submission: TeacherFormSubmission;
}

export function SubmissionDataView({ submission }: SubmissionDataViewProps) {
  const { t } = useTranslation("teacherFormSubmissions");
  const [schools, setSchools] = useState<School[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Always load schools and subjects for lookup (needed for both submitted and invited states)
    const loadLookups = async () => {
      try {
        const [schoolsRes, subjectsRes] = await Promise.all([
          SchoolService.list({ page: 1, pageSize: 1000 }),
          SubjectService.getAll(),
        ]);
        setSchools(schoolsRes.items || []);
        setSubjects(subjectsRes || []);
      } catch (error) {
        console.error("Failed to load lookup data:", error);
      } finally {
        setLoading(false);
      }
    };

    loadLookups();
  }, []);

  const getSchoolName = (schoolId: number | null | undefined): string => {
    if (!schoolId) return "-";
    const school = schools.find((s) => s.id === schoolId);
    return school?.schoolName || `School ID: ${schoolId}`;
  };

  const getSubjectNames = (subjectIds: number[]): string[] => {
    return subjectIds
      .map((id) => {
        const subject = subjects.find((s) => s.id === id);
        return subject ? `${subject.subjectCode} - ${subject.subjectTitle}` : `Subject ID: ${id}`;
      })
      .filter(Boolean);
  };

  const internshipTypeLabels: Record<string, string> = {
    only_pdp: "Only PDP internships (PDP I + PDP II)",
    only_wednesday: "Only ZSP/SFP (Wednesday internships)",
    mixed: "Mixed (PDP + Wednesday)",
    specific: "Specific internship combinations",
  };

  const combinationLabels: Record<string, string> = {
    pdp1_pdp2: "PDP I + PDP II",
    pdp1_sfp: "PDP I + SFP",
    pdp1_zsp: "PDP I + ZSP",
    pdp2_sfp: "PDP II + SFP",
    pdp2_zsp: "PDP II + ZSP",
    sfp_zsp: "SFP + ZSP",
  };

  const semesterLabels: Record<string, string> = {
    autumn: "Autumn (for PDP I)",
    spring: "Spring (for PDP II)",
    winter_wednesday: "Winter semester Wednesdays (ZSP)",
    summer_wednesday: "Summer semester Wednesdays (SFP)",
  };

  const availabilityLabels: Record<string, string> = {
    morning: "Morning availability",
    afternoon: "Afternoon availability",
    full_day: "Full day availability",
    flexible: "Flexible schedule",
    specific_days: "Specific days only",
  };

  return (
    <div className="space-y-4 py-4">
      {/* Teacher Info */}
      <div className="grid gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <label className="text-sm font-medium">{t("form.fields.teacher")}</label>
          <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
            <Link
              to={`${ROUTES.baseData.teachers}?teacherId=${submission.teacherId}`}
              className="text-primary hover:underline font-medium"
            >
              {submission.teacherFirstName} {submission.teacherLastName}
            </Link>
            <div className="text-xs mt-1">{submission.teacherEmail}</div>
          </div>
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t("form.fields.academicYear")}</label>
          <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
            {submission.yearName}
          </div>
        </div>
        <div className="space-y-2 md:col-span-2">
          <label className="text-sm font-medium">{t("form.fields.formToken")}</label>
          <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50 font-mono break-all overflow-wrap-anywhere">
            {submission.formToken || "-"}
          </div>
        </div>
        {submission.submittedAt && (
          <div className="space-y-2">
            <label className="text-sm font-medium">{t("form.fields.submittedAt")}</label>
            <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
              {new Date(submission.submittedAt).toLocaleString()}
            </div>
          </div>
        )}
        <div className="space-y-2">
          <label className="text-sm font-medium">{t("form.fields.status")}</label>
          <div className="p-2">
            {submission.isProcessed ? (
              <Badge variant="success" className="flex items-center gap-1 w-fit">
                <CheckCircle2 className="h-3 w-3" />
                {t("form.fields.processed")}
              </Badge>
            ) : (
              <Badge variant="secondary" className="flex items-center gap-1 w-fit">
                <XCircle className="h-3 w-3" />
                {t("form.fields.unprocessed")}
              </Badge>
            )}
          </div>
        </div>
      </div>

      {/* Form Submission Data */}
      {loading ? (
        <div className="flex items-center justify-center py-8 border-t pt-4">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
        </div>
      ) : (
        <div className="space-y-4 border-t pt-4">
          <h3 className="text-lg font-semibold">{t("form.submissionDetails")}</h3>
          
          <div className="grid gap-4 md:grid-cols-2">
            {/* School */}
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("publicForm.school")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.schoolId ? getSchoolName(submission.schoolId) : "-"}
              </div>
            </div>

            {/* Employment Status */}
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("publicForm.employmentStatus")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.employmentStatus 
                  ? t(`publicForm.employmentStatusOptions.${submission.employmentStatus}`)
                  : "-"}
              </div>
            </div>

            {/* Subjects */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.subjects")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.subjectIds && submission.subjectIds.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {getSubjectNames(submission.subjectIds).map((name, idx) => (
                      <Badge key={idx} variant="secondary">
                        {name}
                      </Badge>
                    ))}
                  </div>
                ) : (
                  <span className="text-muted-foreground">-</span>
                )}
              </div>
            </div>

            {/* Availability Options */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.availabilityOptions")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.availabilityOptions && submission.availabilityOptions.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {submission.availabilityOptions.map((opt) => (
                      <Badge key={opt} variant="outline">
                        {availabilityLabels[opt] || opt}
                      </Badge>
                    ))}
                  </div>
                ) : (
                  <span className="text-muted-foreground">-</span>
                )}
              </div>
            </div>

            {/* Internship Type Preference */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.internshipTypes")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.internshipTypePreference 
                  ? (internshipTypeLabels[submission.internshipTypePreference] || submission.internshipTypePreference)
                  : "-"}
              </div>
            </div>

            {/* Specific Combinations */}
            {submission.internshipTypePreference === "specific" && (
              <div className="space-y-2 md:col-span-2">
                <label className="text-sm font-medium">{t("publicForm.selectCombinations")}</label>
                <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                  {submission.internshipCombinations && submission.internshipCombinations.length > 0 ? (
                    <div className="flex flex-wrap gap-2">
                      {submission.internshipCombinations.map((combo) => (
                        <Badge key={combo} variant="outline">
                          {combinationLabels[combo] || combo}
                        </Badge>
                      ))}
                    </div>
                  ) : (
                    <span className="text-muted-foreground">-</span>
                  )}
                </div>
              </div>
            )}

            {/* Semester Availability */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.semesterAvailability")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.semesterAvailability && submission.semesterAvailability.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {submission.semesterAvailability.map((sem) => (
                      <Badge key={sem} variant="outline">
                        {semesterLabels[sem] || sem}
                      </Badge>
                    ))}
                  </div>
                ) : (
                  <span className="text-muted-foreground">-</span>
                )}
              </div>
            </div>

            {/* Notes */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.notes")}</label>
              <div className="text-sm text-muted-foreground p-3 border rounded-md bg-muted/50 whitespace-pre-wrap">
                {submission.notes || <span className="text-muted-foreground">-</span>}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

