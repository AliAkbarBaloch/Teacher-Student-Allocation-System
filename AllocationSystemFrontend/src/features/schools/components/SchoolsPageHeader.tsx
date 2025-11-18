// components
import { Button } from "@/components/ui/button";
// icons
import { Plus, ShieldAlert } from "lucide-react";

// types
interface SchoolsPageHeaderProps {
  isAdmin: boolean;
  title: string;
  subtitle: string;
  createLabel: string;
  readOnlyTitle: string;
  readOnlyDescription: string;
  onCreate: () => void;
}

export function SchoolsPageHeader({
  isAdmin,
  title,
  subtitle,
  createLabel,
  readOnlyTitle,
  readOnlyDescription,
  onCreate,
}: SchoolsPageHeaderProps) {
  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
          <p className="text-sm text-muted-foreground">{subtitle}</p>
        </div>
        {isAdmin && (
          <Button onClick={onCreate} className="gap-2">
            <Plus className="h-4 w-4" />
            {createLabel}
          </Button>
        )}
      </div>

      {!isAdmin && (
        <div className="flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900 dark:border-amber-400/30 dark:bg-amber-500/10 dark:text-amber-100">
          <ShieldAlert className="mt-0.5 h-4 w-4" />
          <div>
            <p className="font-medium">{readOnlyTitle}</p>
            <p>{readOnlyDescription}</p>
          </div>
        </div>
      )}
    </div>
  );
}

