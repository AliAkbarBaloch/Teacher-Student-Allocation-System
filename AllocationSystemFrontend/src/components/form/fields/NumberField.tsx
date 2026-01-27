import { Input } from "@/components/ui/input";
import React from "react";

interface NumberFieldProps {
  id: string;
  label: string;
  value: number | string;
  onChange: (value: string | number) => void;
  placeholder?: string;
  required?: boolean;
  error?: string;
  disabled?: boolean;
  min?: number;
  max?: number; // NEW
  step?: number | string; // NEW
  type?: string; // NEW, defaults to "number"
  className?: string;
  labelClassName?: string;
}

export const NumberField: React.FC<NumberFieldProps> = ({
  id,
  label,
  value,
  onChange,
  placeholder,
  required = false,
  error,
  disabled,
  min = 0,
  max,
  step,
  type = "number",
  className = "",
  labelClassName = "text-sm font-medium",
}) => (
  <div className="space-y-2">
    <label htmlFor={id} className={labelClassName}>
      {label}
      {required && <span className="text-destructive ml-1">*</span>}
    </label>
    <Input
      id={id}
      type={type}
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder={placeholder}
      disabled={disabled}
      className={error ? `border-destructive ${className}` : className}
      min={min}
      max={max}
      step={step || "any"}
    />
    {error && <p className="text-sm text-destructive">{error}</p>}
  </div>
);