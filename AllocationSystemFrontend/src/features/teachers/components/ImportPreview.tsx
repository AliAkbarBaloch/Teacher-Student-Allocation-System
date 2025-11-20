import { useMemo } from "react";
import { AlertCircle, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";
import type { ParsedTeacherRow, RowValidationError } from "../types/teacher.types";

interface ImportPreviewProps {
  data: ParsedTeacherRow[];
  errors: RowValidationError[];
  onConfirm: () => void;
  onCancel: () => void;
  isLoading?: boolean;
}

export function ImportPreview({
  data,
  errors,
  onConfirm,
  onCancel,
  isLoading = false,
}: ImportPreviewProps) {
  const errorMap = useMemo(() => {
    const map = new Map<number, RowValidationError[]>();
    errors.forEach((error) => {
      const existing = map.get(error.rowNumber) || [];
      existing.push(error);
      map.set(error.rowNumber, existing);
    });
    return map;
  }, [errors]);

  const validCount = data.filter((row) => !errorMap.has(row.rowNumber)).length;
  const invalidCount = data.length - validCount;

  const getRowErrors = (rowNumber: number): RowValidationError[] => {
    return errorMap.get(rowNumber) || [];
  };

  const formatToTitleCase = (value: string): string => {
    return value
      .split("_")
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(" ");
  };

  return (
    <div className="space-y-4">
      {/* Summary */}
      <div className="flex items-center justify-between p-4 bg-muted/30 rounded-lg">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="h-5 w-5 text-green-600" />
            <span className="text-sm font-medium">
              {validCount} valid {validCount === 1 ? "row" : "rows"}
            </span>
          </div>
          {invalidCount > 0 && (
            <div className="flex items-center gap-2">
              <AlertCircle className="h-5 w-5 text-destructive" />
              <span className="text-sm font-medium text-destructive">
                {invalidCount} invalid {invalidCount === 1 ? "row" : "rows"}
              </span>
            </div>
          )}
        </div>
        <div className="text-sm text-muted-foreground">
          Total: {data.length} {data.length === 1 ? "row" : "rows"}
        </div>
      </div>

      {/* Preview Table */}
      <div className="border rounded-lg overflow-hidden max-h-[400px] overflow-y-auto">
        <Table>
          <TableHeader className="sticky top-0 bg-background z-10">
            <TableRow>
              <TableHead className="w-16">Row</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>School</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Employment</TableHead>
              <TableHead>Part Time</TableHead>
              <TableHead>Errors</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {data.map((row) => {
              const rowErrors = getRowErrors(row.rowNumber);
              const isValid = rowErrors.length === 0;

              return (
                <TableRow
                  key={row.rowNumber}
                  className={cn(
                    isValid ? "bg-background" : "bg-destructive/5",
                    "hover:bg-muted/50"
                  )}
                >
                  <TableCell className="font-mono text-xs">{row.rowNumber}</TableCell>
                  <TableCell>
                    {isValid ? (
                      <Badge variant="default" className="bg-green-600">
                        Valid
                      </Badge>
                    ) : (
                      <Badge variant="destructive">Invalid</Badge>
                    )}
                  </TableCell>
                  <TableCell className="text-sm">
                    {row.schoolName || (row.schoolId ? `ID: ${row.schoolId}` : "-")}
                  </TableCell>
                  <TableCell className="text-sm">
                    {row.firstName} {row.lastName}
                  </TableCell>
                  <TableCell className="text-sm font-mono">{row.email}</TableCell>
                  <TableCell className="text-sm">
                    {formatToTitleCase(row.employmentStatus)}
                  </TableCell>
                  <TableCell className="text-sm">
                    {row.isPartTime ? (
                      <Badge variant="secondary">Yes</Badge>
                    ) : (
                      <Badge variant="outline">No</Badge>
                    )}
                  </TableCell>
                  <TableCell className="max-w-xs">
                    {rowErrors.length > 0 ? (
                      <div className="space-y-1">
                        {rowErrors.slice(0, 2).map((error, idx) => (
                          <p key={idx} className="text-xs text-destructive">
                            {error.field}: {error.message}
                          </p>
                        ))}
                        {rowErrors.length > 2 && (
                          <p className="text-xs text-muted-foreground">
                            +{rowErrors.length - 2} more
                          </p>
                        )}
                      </div>
                    ) : (
                      <span className="text-xs text-muted-foreground">-</span>
                    )}
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </div>

      {/* Actions */}
      <div className="flex items-center justify-between pt-4 border-t">
        <div className="text-sm text-muted-foreground">
          {invalidCount > 0 && (
            <p>
              Please fix {invalidCount} invalid {invalidCount === 1 ? "row" : "rows"} before
              importing.
            </p>
          )}
          {invalidCount === 0 && <p>All rows are valid. Ready to import.</p>}
        </div>
        <div className="flex items-center gap-2">
          <Button type="button" variant="outline" onClick={onCancel} disabled={isLoading}>
            Cancel
          </Button>
          <Button
            type="button"
            onClick={onConfirm}
            disabled={isLoading || invalidCount > 0}
          >
            {isLoading ? "Importing..." : `Import ${validCount} ${validCount === 1 ? "Row" : "Rows"}`}
          </Button>
        </div>
      </div>
    </div>
  );
}

