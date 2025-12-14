// react
import { useState, useEffect, useMemo } from "react";
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
import { InternshipTypeService } from "@/features/internship-types/services/internshipTypeService";
// types
import type { TeacherFormSubmission } from "../types/teacherFormSubmission.types";
import type { School } from "@/features/schools/types/school.types";
import type { Subject } from "@/features/subjects/types/subject.types";
import type { InternshipType } from "@/features/internship-types/types/internshipType.types";

interface SubmissionDataViewProps {
  submission: TeacherFormSubmission;
}

export function SubmissionDataView({ submission }: SubmissionDataViewProps) {
  const { t } = useTranslation("teacherSubmissions");
  const [schools, setSchools] = useState<School[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [internshipTypes, setInternshipTypes] = useState<InternshipType[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Load schools, subjects, and internship types for lookup
    const loadLookups = async () => {
      try {
        const [schoolsRes, subjectsRes, internshipTypesRes] = await Promise.all([
          SchoolService.getPaginated({ page: 1, pageSize: 1000 }),
          SubjectService.getAll(),
          InternshipTypeService.getAll(),
        ]);
        setSchools(schoolsRes.items || []);
        setSubjects(subjectsRes || []);
        setInternshipTypes(internshipTypesRes || []);
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

  const getSchoolType = (schoolId: number | null | undefined): string => {
    if (!schoolId) return "-";
    const school = schools.find((s) => s.id === schoolId);
    if (!school) return "-";
    return t(`publicForm.schoolTypeOptions.${school.schoolType}`);
  };

  // Memoize helper functions to avoid recalculating on every render
  const subjectNames = useMemo(() => {
    if (!submission.subjectIds || submission.subjectIds.length === 0) return [];
    return submission.subjectIds
      .map((id) => {
        const subject = subjects.find((s) => s.id === id);
        return subject ? `${subject.subjectCode} - ${subject.subjectTitle}` : `Subject ID: ${id}`;
      })
      .filter(Boolean);
  }, [submission.subjectIds, subjects]);

  // Parse internship type IDs from internshipCombinations field
  // Backend stores IDs as comma-separated string "1,2,3" which gets parsed to ["1", "2", "3"]
  const internshipTypeIds = useMemo(() => {
    if (!submission.internshipCombinations || submission.internshipCombinations.length === 0) {
      return [];
    }
    // internshipCombinations is an array of strings (parsed from comma-separated string)
    // Each element is an ID as a string
    return submission.internshipCombinations
      .map((id) => parseInt(id.trim(), 10))
      .filter((id) => !isNaN(id));
  }, [submission.internshipCombinations]);

  const internshipTypeNames = useMemo(() => {
    return internshipTypeIds
      .map((id) => {
        const type = internshipTypes.find((t) => t.id === id);
        return type ? `${type.internshipCode} - ${type.fullName}` : `Internship Type ID: ${id}`;
      })
      .filter(Boolean);
  }, [internshipTypeIds, internshipTypes]);

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
            {/* School Type */}
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("publicForm.schoolType")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.schoolId ? getSchoolType(submission.schoolId) : "-"}
              </div>
            </div>

            {/* School Name */}
            <div className="space-y-2">
              <label className="text-sm font-medium">{t("publicForm.school")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {submission.schoolId ? getSchoolName(submission.schoolId) : "-"}
              </div>
            </div>

            {/* Subjects */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.subjects")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {subjectNames.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {subjectNames.map((name, idx) => (
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

            {/* Internship Types */}
            <div className="space-y-2 md:col-span-2">
              <label className="text-sm font-medium">{t("publicForm.internshipTypes")}</label>
              <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {internshipTypeNames.length > 0 ? (
                  <div className="flex flex-wrap gap-2">
                    {internshipTypeNames.map((name, idx) => (
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

