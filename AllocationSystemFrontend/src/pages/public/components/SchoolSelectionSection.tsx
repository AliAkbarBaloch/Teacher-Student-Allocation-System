import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { School } from "@/features/schools/types/school.types";

interface SchoolSelectionSectionProps {
  schoolId: number | null;
  schools: School[];
  loading: boolean;
  onChange: (schoolId: number) => void;
  t: (key: string) => string;
}

export function SchoolSelectionSection({
  schoolId,
  schools,
  loading,
  onChange,
  t,
}: SchoolSelectionSectionProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor="school">
        {t("publicForm.school")} *
      </Label>
      <Select
        value={schoolId?.toString() || ""}
        onValueChange={(value) => onChange(Number(value))}
        disabled={loading}
      >
        <SelectTrigger id="school">
          <SelectValue placeholder={t("publicForm.selectSchool")} />
        </SelectTrigger>
        <SelectContent>
          {schools.map((school) => (
            <SelectItem key={school.id} value={school.id.toString()}>
              {school.schoolName}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

