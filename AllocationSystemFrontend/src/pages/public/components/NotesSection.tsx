import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

interface NotesSectionProps {
  notes: string;
  onChange: (notes: string) => void;
  t: (key: string) => string;
}

export function NotesSection({ notes, onChange, t }: NotesSectionProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor="notes">{t("publicForm.notes")}</Label>
      <Textarea
        id="notes"
        value={notes}
        onChange={(e) => onChange(e.target.value)}
        placeholder={t("publicForm.notesPlaceholder")}
        rows={4}
        maxLength={5000}
      />
      <p className="text-sm text-muted-foreground">{t("publicForm.notesHint")}</p>
    </div>
  );
}

