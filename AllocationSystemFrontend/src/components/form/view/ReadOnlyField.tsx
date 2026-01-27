import React from "react";

/**
 * Read only field component Props
 */
interface ReadOnlyFieldProps {
  label: React.ReactNode;
  value: React.ReactNode;
  className?: string;
  labelClassName?: string;
  valueClassName?: string;
}

export const ReadOnlyField: React.FC<ReadOnlyFieldProps> = ({
  label,
  value,
  className = "space-y-2",
  labelClassName = "text-sm font-medium",
  valueClassName = "text-sm text-muted-foreground p-2 border rounded-md bg-muted/50",
}) => (
  <div className={className}>
    <label className={labelClassName}>{label}</label>
    <div className={valueClassName}>{value}</div>
  </div>
);