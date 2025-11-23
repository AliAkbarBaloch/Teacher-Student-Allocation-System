import { Label } from "@/components/ui/label";
import { MultiSelect } from "@/components/ui/multi-select";
import type { Subject } from "@/features/subjects/types/subject.types";

interface SubjectSelectionSectionProps {
  subjectIds: number[];
  subjects: Subject[];
  loading: boolean;
  onChange: (subjectIds: number[]) => void;
  t: (key: string) => string;
}

export function SubjectSelectionSection({
  subjectIds,
  subjects,
  loading,
  onChange,
  t,
}: SubjectSelectionSectionProps) {
  return (
    <div className="space-y-2">
      <Label>{t("publicForm.subjects")} *</Label>
      <MultiSelect
        options={subjects.map((s) => ({
          label: `${s.subjectCode} - ${s.subjectTitle}`,
          value: s.id,
        }))}
        selected={subjectIds}
        onChange={(selected) => onChange(selected as number[])}
        placeholder={t("publicForm.selectSubjects")}
        disabled={loading}
      />
      <p className="text-sm text-muted-foreground">{t("publicForm.subjectsHint")}</p>
    </div>
  );
}

