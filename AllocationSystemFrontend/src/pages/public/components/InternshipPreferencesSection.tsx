import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Checkbox } from "@/components/ui/checkbox";

const INTERNSHIP_TYPE_OPTIONS = [
  { value: "only_pdp", label: "Only PDP internships (PDP I + PDP II)" },
  { value: "only_wednesday", label: "Only ZSP/SFP (Wednesday internships)" },
  { value: "mixed", label: "Mixed (PDP + Wednesday)" },
  { value: "specific", label: "Specific internship combinations" },
];

const INTERNSHIP_COMBINATIONS = [
  { value: "pdp1_pdp2", label: "PDP I + PDP II" },
  { value: "pdp1_sfp", label: "PDP I + SFP" },
  { value: "pdp1_zsp", label: "PDP I + ZSP" },
  { value: "pdp2_sfp", label: "PDP II + SFP" },
  { value: "pdp2_zsp", label: "PDP II + ZSP" },
  { value: "sfp_zsp", label: "SFP + ZSP" },
];

interface InternshipPreferencesSectionProps {
  internshipTypePreference: string;
  internshipCombinations: string[];
  onTypeChange: (type: string) => void;
  onCombinationsChange: (combinations: string[]) => void;
  t: (key: string) => string;
}

export function InternshipPreferencesSection({
  internshipTypePreference,
  internshipCombinations,
  onTypeChange,
  onCombinationsChange,
  t,
}: InternshipPreferencesSectionProps) {
  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label>{t("publicForm.internshipTypes")} *</Label>
        <RadioGroup
          value={internshipTypePreference}
          onValueChange={(value) => {
            onTypeChange(value);
            if (value !== "specific") {
              onCombinationsChange([]);
            }
          }}
        >
          {INTERNSHIP_TYPE_OPTIONS.map((option) => (
            <div key={option.value} className="flex items-center space-x-2">
              <RadioGroupItem value={option.value} id={`internship-${option.value}`} />
              <label
                htmlFor={`internship-${option.value}`}
                className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
              >
                {option.label}
              </label>
            </div>
          ))}
        </RadioGroup>
      </div>

      {internshipTypePreference === "specific" && (
        <div className="space-y-2 pl-6 border-l-2">
          <Label className="text-sm">{t("publicForm.selectCombinations")} *</Label>
          <div className="space-y-2">
            {INTERNSHIP_COMBINATIONS.map((combination) => (
              <div key={combination.value} className="flex items-center space-x-2">
                <Checkbox
                  id={`combination-${combination.value}`}
                  checked={internshipCombinations.includes(combination.value)}
                  onCheckedChange={(checked) => {
                    if (checked) {
                      onCombinationsChange([...internshipCombinations, combination.value]);
                    } else {
                      onCombinationsChange(
                        internshipCombinations.filter((v) => v !== combination.value)
                      );
                    }
                  }}
                />
                <label
                  htmlFor={`combination-${combination.value}`}
                  className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70 cursor-pointer"
                >
                  {combination.label}
                </label>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

