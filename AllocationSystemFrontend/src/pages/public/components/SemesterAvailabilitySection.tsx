import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";

const SEMESTER_OPTIONS = [
  { value: "autumn", label: "Autumn (for PDP I)" },
  { value: "spring", label: "Spring (for PDP II)" },
  { value: "winter_wednesday", label: "Winter semester Wednesdays (ZSP)" },
  { value: "summer_wednesday", label: "Summer semester Wednesdays (SFP)" },
];

interface SemesterAvailabilitySectionProps {
  semesterAvailability: string[];
  onChange: (options: string[]) => void;
  t: (key: string) => string;
}

export function SemesterAvailabilitySection({
  semesterAvailability,
  onChange,
  t,
}: SemesterAvailabilitySectionProps) {
  return (
    <div className="space-y-2">
      <Label>{t("publicForm.semesterAvailability")} *</Label>
      <div className="space-y-2">
        {SEMESTER_OPTIONS.map((option) => (
          <div key={option.value} className="flex items-center space-x-2">
            <Checkbox
              id={`semester-${option.value}`}
              checked={semesterAvailability.includes(option.value)}
              onCheckedChange={(checked) => {
                if (checked) {
                  onChange([...semesterAvailability, option.value]);
                } else {
                  onChange(semesterAvailability.filter((v) => v !== option.value));
                }
              }}
            />
            <label
              htmlFor={`semester-${option.value}`}
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
    </div>
  );
}

