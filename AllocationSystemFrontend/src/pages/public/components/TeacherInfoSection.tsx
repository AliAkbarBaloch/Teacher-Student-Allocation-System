import { Label } from "@/components/ui/label";
import type { FormLinkResponse } from "@/features/teacher-form-submissions/types/teacherFormSubmission.types";

interface TeacherInfoSectionProps {
  formDetails: FormLinkResponse;
  t: (key: string) => string;
}

export function TeacherInfoSection({ formDetails, t }: TeacherInfoSectionProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <div className="space-y-2">
        <Label>{t("publicForm.teacher")}</Label>
        <div className="p-3 bg-muted rounded-md">
          <p className="font-medium">{formDetails.teacherName}</p>
          <p className="text-sm text-muted-foreground">{formDetails.teacherEmail}</p>
        </div>
      </div>
      <div className="space-y-2">
        <Label>{t("publicForm.academicYear")}</Label>
        <div className="p-3 bg-muted rounded-md">
          <p className="font-medium">{formDetails.yearName}</p>
        </div>
      </div>
    </div>
  );
}

