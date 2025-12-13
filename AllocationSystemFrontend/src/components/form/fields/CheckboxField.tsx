import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import React from "react";

interface CheckboxFieldProps {
  id: string;
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  label: React.ReactNode;
  description?: React.ReactNode;
  statusText?: React.ReactNode;
  disabled?: boolean;
  className?: string;
  labelClassName?: string;
}

export const CheckboxField: React.FC<CheckboxFieldProps> = ({
  id,
  checked,
  onCheckedChange,
  label,
  description,
  statusText,
  disabled,
  className = "hover:bg-accent/50 flex items-start gap-3 rounded-lg border p-4 cursor-pointer has-[[aria-checked=true]]:border-primary has-[[aria-checked=true]]:bg-primary/10 transition-colors",
  labelClassName = "text-sm font-medium leading-none",
}) => (
  <Label htmlFor={id} className={className}>
    <Checkbox
      id={id}
      checked={checked}
      onCheckedChange={val => onCheckedChange(val === true)}
      disabled={disabled}
      className="h-5 w-5 mt-0.5 data-[state=checked]:border-primary data-[state=checked]:bg-primary"
    />
    <div className="grid gap-1.5 flex-1">
      <p className={labelClassName}>{label}</p>
      {description && <p className="text-xs text-muted-foreground">{description}</p>}
      {statusText && <p className="text-sm text-muted-foreground mt-1">{statusText}</p>}
    </div>
  </Label>
);