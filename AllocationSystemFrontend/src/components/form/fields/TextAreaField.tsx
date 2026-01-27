import React from "react";

/**
 * Text area field component Props
 */
interface TextAreaFieldProps {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  error?: string;
  disabled?: boolean;
  rows?: number;
  className?: string;
  maxLength?: number;
  labelClassName?: string;
}

export const TextAreaField: React.FC<TextAreaFieldProps> = ({
  id,
  label,
  value,
  onChange,
  placeholder,
  required = false,
  error,
  disabled,
  rows = 3,
  className = "",
  maxLength,
  labelClassName = "text-sm font-medium",
}) => (
  <div className="space-y-2">
    <label htmlFor={id} className={labelClassName}>
      {label}
      {required && <span className="text-destructive ml-1">*</span>}
    </label>
    <textarea
      id={id}
      rows={rows}
      value={value}
      placeholder={placeholder}
      onChange={e => onChange(e.target.value)}
      disabled={disabled}
      maxLength={maxLength}
      className={`flex min-h-[90px] w-full rounded-md border bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${error ? "border-destructive" : "border-input"} ${className}`}
    />
    {error && <p className="text-sm text-destructive">{error}</p>}
  </div>
);