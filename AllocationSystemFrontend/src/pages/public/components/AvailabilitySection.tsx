import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";

const AVAILABILITY_OPTIONS = [
  { value: "morning", label: "Morning availability" },
  { value: "afternoon", label: "Afternoon availability" },
  { value: "full_day", label: "Full day availability" },
  { value: "flexible", label: "Flexible schedule" },
  { value: "specific_days", label: "Specific days only" },
];

interface AvailabilitySectionProps {
  availabilityOptions: string[];
  onChange: (options: string[]) => void;
  t: (key: string, options?: Record<string, unknown>) => string;
}

export function AvailabilitySection({
  availabilityOptions,
  onChange,
  t,
}: AvailabilitySectionProps) {
  return (
    <div className="space-y-2">
      <Label>{t("publicForm.availabilityOptions")} *</Label>
      <p className="text-sm text-muted-foreground mb-2">
        {t("publicForm.availabilityOptionsHint")}
      </p>
      <div className="space-y-2">
        {AVAILABILITY_OPTIONS.map((option) => (
          <div key={option.value} className="flex items-center space-x-2">
            <Checkbox
              id={`availability-${option.value}`}
              checked={availabilityOptions.includes(option.value)}
              onCheckedChange={(checked) => {
                if (checked) {
                  onChange([...availabilityOptions, option.value]);
                } else {
                  onChange(availabilityOptions.filter((v) => v !== option.value));
                }
              }}
            />
            <label
              htmlFor={`availability-${option.value}`}
              className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
      {availabilityOptions.length < 2 && availabilityOptions.length > 0 && (
        <p className="text-sm text-amber-600">
          {t("publicForm.availabilityOptionsMin", {
            count: 2 - availabilityOptions.length,
          })}
        </p>
      )}
    </div>
  );
}

