import { Badge } from "@/components/ui/badge";
import type { ComponentType } from "react";
import { cn } from "@/lib/utils";

interface StatusItemProps {
  icon: ComponentType<{ className?: string }>;
  iconColor: string;
  label: string;
  value: number;
  percentage?: number;
  bgColor: string;
  badgeColor: string;
  showPercentage?: boolean;
  percentageLabel?: string;
}

/**
 * Reusable status item component for displaying status with icon, count, and percentage
 */
export function StatusItem({
  icon: Icon,
  iconColor,
  label,
  value,
  percentage,
  bgColor,
  badgeColor,
  showPercentage = true,
  percentageLabel,
}: StatusItemProps) {
  return (
    <div className={cn("flex items-center justify-between p-3 rounded-lg border", bgColor)}>
      <div className="flex items-center gap-3">
        <Icon className={cn("h-5 w-5", iconColor)} />
        <div>
          <p className="text-sm font-medium">{label}</p>
          {showPercentage && percentage !== undefined && (
            <p className="text-xs text-muted-foreground">
              {percentageLabel || `${percentage}% of total`}
            </p>
          )}
        </div>
      </div>
      <Badge className={badgeColor}>{value}</Badge>
    </div>
  );
}
