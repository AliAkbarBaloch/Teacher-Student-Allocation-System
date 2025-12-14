import React, { useState, useRef, useEffect } from "react";

interface SelectFieldOption {
  value: string;
  label: React.ReactNode;
  disabled?: boolean;
}

interface SelectSearchFieldProps {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  options: SelectFieldOption[];
  placeholder?: string;
  required?: boolean;
  error?: string;
  disabled?: boolean;
  className?: string;
  labelClassName?: string;
}

export const SelectSearchField: React.FC<SelectSearchFieldProps> = ({
  id,
  label,
  value,
  onChange,
  options,
  placeholder,
  required = false,
  error,
  disabled,
  className = "w-full",
  labelClassName = "text-sm font-medium",
}) => {
  const [search, setSearch] = useState("");
  const [open, setOpen] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const filteredOptions = options.filter(
    (opt) =>
      !search ||
      String(opt.label).toLowerCase().includes(search.toLowerCase())
  );

  // Close dropdown on outside click
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (
        inputRef.current &&
        !inputRef.current.parentElement?.contains(e.target as Node)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [open]);

  // Set search to selected label when value changes
  useEffect(() => {
    const selected = options.find((opt) => opt.value === value);
    setSearch(selected ? String(selected.label) : "");
  }, [value, options]);

  return (
    <div className="space-y-2">
      <label htmlFor={id} className={labelClassName}>
        {label}
        {required && <span className="text-destructive ml-1">*</span>}
      </label>
      <div className={`relative ${className}`}>
        <input
          id={id}
          ref={inputRef}
          type="text"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setOpen(true);
          }}
          onFocus={() => setOpen(true)}
          placeholder={placeholder}
          disabled={disabled}
          className={`w-full border rounded px-2 py-2 text-sm bg-background ${error ? "border-destructive" : "border-input"} ${disabled ? "opacity-50" : ""}`}
          autoComplete="off"
        />
        {open && !disabled && (
          <div className="absolute z-10 mt-1 w-full bg-background border rounded shadow max-h-56 overflow-auto">
            {filteredOptions.length === 0 ? (
              <div className="p-2 text-muted-foreground text-sm">No options</div>
            ) : (
              filteredOptions.map((opt) => (
                <div
                  key={opt.value}
                  className={`p-2 text-sm cursor-pointer hover:bg-muted ${opt.disabled ? "opacity-50 pointer-events-none" : ""} ${opt.value === value ? "bg-muted" : ""}`}
                  onMouseDown={() => {
                    if (!opt.disabled) {
                      onChange(opt.value);
                      setOpen(false);
                    }
                  }}
                >
                  {opt.label}
                </div>
              ))
            )}
          </div>
        )}
      </div>
      {error && <p className="text-sm text-destructive">{error}</p>}
    </div>
  );
};