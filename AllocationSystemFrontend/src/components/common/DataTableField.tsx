import { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import type { ColumnConfig, FieldType } from "@/types/datatable.types";

/**
 * Props for the DataTableField component.
 */
interface DataTableFieldProps<TData> {
    field: string;
    value: unknown;
    config?: ColumnConfig;
    isEditing: boolean;
    editingRow: Partial<TData>;
    setEditingRow: (row: Partial<TData>) => void;
}

/**
 * Renders a single field in the DataTableDialog, either as a display value or an editable input.
 */
export function DataTableField<TData>({
    field,
    value,
    config,
    isEditing,
    editingRow,
    setEditingRow,
}: DataTableFieldProps<TData>) {
    // Local state to handle string values for numeric/text inputs to avoid dot-stripping and validation issues while typing
    const [localValue, setLocalValue] = useState<string>(
        value !== null && value !== undefined ? String(value) : ""
    );

    // Sync local value when external value changes (but not while typing if possible)
    useEffect(() => {
        const stringValue = value !== null && value !== undefined ? String(value) : "";
        // Only update if the numeric value actually changed (to avoid jitter while typing 10.0)
        setLocalValue((prev) => {
            if (Number(prev) !== Number(stringValue) || (prev === "" && stringValue !== "")) {
                return stringValue;
            }
            return prev;
        });
    }, [value]);

    if (!isEditing) {
        return (
            <div className="text-sm text-muted-foreground p-2 border rounded-md bg-muted/50">
                {String(value ?? "")}
            </div>
        );
    }

    const fieldType: FieldType = config?.fieldType || "text";
    const isDisabled = config?.fieldDisabled || config?.fieldReadOnly || field === "id";
    const placeholder = config?.fieldPlaceholder || "";

    const handleChange = (val: string) => {
        setLocalValue(val);
        setEditingRow({ ...editingRow, [field]: val });
    };

    switch (fieldType) {
        case "textarea":
            return (
                <textarea
                    value={localValue}
                    onChange={(e) => handleChange(e.target.value)}
                    disabled={isDisabled}
                    placeholder={placeholder}
                    className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                    rows={4}
                />
            );

        case "select":
            if (!config?.fieldOptions) {
                return (
                    <Input
                        value={localValue}
                        onChange={(e) => handleChange(e.target.value)}
                        disabled={isDisabled}
                        placeholder={placeholder}
                    />
                );
            }
            return (
                <Select
                    value={localValue}
                    onValueChange={handleChange}
                    disabled={isDisabled}
                >
                    <SelectTrigger>
                        <SelectValue placeholder={placeholder || "Select an option"} />
                    </SelectTrigger>
                    <SelectContent>
                        {config.fieldOptions.map((option) => (
                            <SelectItem key={String(option.value)} value={String(option.value)}>
                                {option.label}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>
            );

        case "number":
            return (
                <Input
                    type="number"
                    step="any"
                    inputMode="decimal"
                    value={localValue}
                    onChange={(e) => handleChange(e.target.value)}
                    disabled={isDisabled}
                    placeholder={placeholder}
                />
            );

        case "date":
            return (
                <Input
                    type="date"
                    value={localValue}
                    onChange={(e) => handleChange(e.target.value)}
                    disabled={isDisabled}
                    placeholder={placeholder}
                />
            );

        case "email":
            return (
                <Input
                    type="email"
                    value={localValue}
                    onChange={(e) => handleChange(e.target.value)}
                    disabled={isDisabled}
                    placeholder={placeholder}
                />
            );

        case "url":
            return (
                <Input
                    type="url"
                    value={localValue}
                    onChange={(e) => handleChange(e.target.value)}
                    disabled={isDisabled}
                    placeholder={placeholder}
                />
            );

        default:
            return (
                <Input
                    value={localValue}
                    onChange={(e) => handleChange(e.target.value)}
                    disabled={isDisabled}
                    placeholder={placeholder}
                />
            );
    }
}
