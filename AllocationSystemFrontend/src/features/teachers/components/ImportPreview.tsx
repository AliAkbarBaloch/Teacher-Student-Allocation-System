import { useMemo, useState, useCallback } from "react";
import { AlertCircle, CheckCircle2, ChevronLeft, ChevronRight } from "lucide-react";
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

const ROWS_PER_PAGE = 50; // Limit rendered rows for better performance

export function ImportPreview({
  data,
  errors,
  onConfirm,
  onCancel,
  isLoading = false,
}: ImportPreviewProps) {
  const [currentPage, setCurrentPage] = useState(1);

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

  // Paginate data for better performance
  const totalPages = Math.ceil(data.length / ROWS_PER_PAGE);
  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * ROWS_PER_PAGE;
    const end = start + ROWS_PER_PAGE;
    return data.slice(start, end);
  }, [data, currentPage]);

  const handlePreviousPage = useCallback(() => {
    setCurrentPage((prev) => Math.max(1, prev - 1));
  }, []);

  const handleNextPage = useCallback(() => {
    setCurrentPage((prev) => Math.min(totalPages, prev + 1));
  }, [totalPages]);

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
    <div className="flex flex-col space-y-4 w-full overflow-x-hidden p-4">
      {/* Summary - Fixed header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 sm:gap-4 p-4 bg-muted/30 rounded-lg shrink-0">
        <div className="flex flex-wrap items-center gap-2 sm:gap-4">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="h-5 w-5 text-green-600 shrink-0" />
            <span className="text-sm font-medium">
              {validCount} valid {validCount === 1 ? "row" : "rows"}
            </span>
          </div>
          {invalidCount > 0 && (
            <div className="flex items-center gap-2">
              <AlertCircle className="h-5 w-5 text-destructive shrink-0" />
              <span className="text-sm font-medium text-destructive">
                {invalidCount} invalid {invalidCount === 1 ? "row" : "rows"}
              </span>
            </div>
          )}
        </div>
        <div className="text-sm text-muted-foreground shrink-0">
          Total: {data.length} {data.length === 1 ? "row" : "rows"}
        </div>
      </div>

      {/* Preview Table - Scrollable container for horizontal scroll only */}
      <div className="border rounded-lg overflow-hidden shrink min-h-0 w-full">
        <div className="overflow-x-auto -mx-1 px-1">
          <Table className="min-w-full">
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
              {paginatedData.map((row) => {
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
                              {error.message}
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
      </div>

      {/* Pagination Controls - Fixed footer */}
      {totalPages > 1 && (
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 sm:gap-0 pt-2 border-t shrink-0 w-full overflow-x-hidden">
          <div className="text-sm text-muted-foreground shrink-0">
            Showing {(currentPage - 1) * ROWS_PER_PAGE + 1} to{" "}
            {Math.min(currentPage * ROWS_PER_PAGE, data.length)} of {data.length} rows
          </div>
          <div className="flex items-center justify-center sm:justify-end gap-2 shrink-0">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handlePreviousPage}
              disabled={currentPage === 1}
            >
              <ChevronLeft className="h-4 w-4" />
              <span className="hidden sm:inline">Previous</span>
            </Button>
            <span className="text-sm text-muted-foreground shrink-0">
              Page {currentPage} of {totalPages}
            </span>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleNextPage}
              disabled={currentPage === totalPages}
            >
              <span className="hidden sm:inline">Next</span>
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}

      {/* Actions - Fixed footer */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 sm:gap-0 pt-4 border-t shrink-0 w-full overflow-x-hidden">
        <div className="text-sm text-muted-foreground shrink-0">
          {invalidCount > 0 && (
            <p>
              Please fix {invalidCount} invalid {invalidCount === 1 ? "row" : "rows"} before
              importing.
            </p>
          )}
          {invalidCount === 0 && <p>All rows are valid. Ready to import.</p>}
        </div>
        <div className="flex items-center gap-2 shrink-0 w-full sm:w-auto">
          <Button 
            type="button" 
            variant="outline" 
            onClick={onCancel} 
            disabled={isLoading}
            className="flex-1 sm:flex-initial"
          >
            Cancel
          </Button>
          <Button
            type="button"
            onClick={onConfirm}
            disabled={isLoading || invalidCount > 0}
            className="flex-1 sm:flex-initial"
          >
            {isLoading ? "Importing..." : `Import ${validCount} ${validCount === 1 ? "Row" : "Rows"}`}
          </Button>
        </div>
      </div>
    </div>
  );
}

