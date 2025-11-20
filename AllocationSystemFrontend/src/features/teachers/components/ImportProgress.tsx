import { Loader2 } from "lucide-react";
import { Progress } from "@/components/ui/progress";
import { Button } from "@/components/ui/button";

interface ImportProgressProps {
  progress: number; // 0-100
  current: number;
  total: number;
  status: string;
  onCancel?: () => void;
}

export function ImportProgress({
  progress,
  current,
  total,
  status,
  onCancel,
}: ImportProgressProps) {
  return (
    <div className="space-y-6">
      <div className="space-y-4 text-center">
        <div className="flex items-center justify-center gap-3">
          <Loader2 className="h-6 w-6 animate-spin text-primary" />
          <span className="text-lg font-medium">{status}</span>
        </div>
        {total > 0 && (
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm text-muted-foreground">
              <span>Processing...</span>
              <span>{current} of {total} rows</span>
            </div>
            {progress > 0 && (
              <Progress value={progress} className="h-2" />
            )}
          </div>
        )}
        <p className="text-sm text-muted-foreground">
          This may take a few moments. Please do not close this window.
        </p>
      </div>

      {onCancel && (
        <div className="flex justify-center">
          <Button type="button" variant="outline" onClick={onCancel} size="sm">
            Cancel
          </Button>
        </div>
      )}
    </div>
  );
}

