import { Input } from "@/components/ui/input";
import React from "react";

interface DateTimeFieldProps {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  error?: string;
  disabled?: boolean;
  className?: string;
  labelClassName?: string;
}

export const DateTimeField: React.FC<DateTimeFieldProps> = ({
  id,
  label,
  value,
  onChange,
  placeholder,
  required = false,
  error,
  disabled,
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
      type="datetime-local"
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder={placeholder}
      disabled={disabled}
      className={error ? `border-destructive ${className}` : className}
    />
    {error && <p className="text-sm text-destructive">{error}</p>}
  </div>
);