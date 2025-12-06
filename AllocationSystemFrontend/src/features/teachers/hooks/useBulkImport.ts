import { useState, useCallback } from "react";
import { toast } from "sonner";
import { parseExcelFile } from "../utils/excelParser";
import { validateAllRows } from "../utils/importValidation";
import { TeacherService } from "../services/teacherService";
import { SchoolService } from "@/features/schools/services/schoolService";
import type { School } from "@/features/schools/types/school.types";
import type {
  ParsedTeacherRow,
  ValidationResult,
  BulkImportResponse,
  ImportStep,
} from "../types/teacher.types";

export function useBulkImport(onImportComplete?: () => void) {
  const [step, setStep] = useState<ImportStep>("upload");
  const [file, setFile] = useState<File | null>(null);
  const [parsedData, setParsedData] = useState<ParsedTeacherRow[]>([]);
  const [validationResult, setValidationResult] = useState<ValidationResult | null>(null);
  const [importProgress, setImportProgress] = useState({ current: 0, total: 0, percentage: 0 });
  const [importResults, setImportResults] = useState<BulkImportResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // Load all schools for validation with pagination
  const loadSchools = useCallback(async () => {
    try {
      const allSchools: School[] = [];
      let page = 1;
      const pageSize = 100;
      let hasMore = true;

      while (hasMore) {
        const response = await SchoolService.list({
          isActive: true,
          page,
          pageSize,
          sortBy: "schoolName",
          sortOrder: "asc",
        });
        
        if (response.items && response.items.length > 0) {
          allSchools.push(...response.items);
          hasMore = response.items.length === pageSize && page < (response.totalPages || 1);
          page++;
        } else {
          hasMore = false;
        }
      }

      return allSchools;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Failed to load schools";
      toast.error(message);
      throw err;
    }
  }, []);

  // Handle file selection
  const handleFileSelect = useCallback(async (selectedFile: File) => {
    setFile(selectedFile);
    setError(null);
    setStep("parsing");
    setIsLoading(true);

    try {
      // Parse Excel file
      const parsed = await parseExcelFile(selectedFile);
      setParsedData(parsed);

      // Load schools for validation
      setStep("validating");
      const loadedSchools = await loadSchools();

      // Check existing emails in database
      const emailsToCheck = parsed.map(row => row.email.toLowerCase().trim());
      const existingEmails = await TeacherService.checkExistingEmails(emailsToCheck);

      // Validate data (including database email check)
      const validation = validateAllRows(parsed, loadedSchools, existingEmails);
      setValidationResult(validation);

      setStep("preview");
    } catch (err) {
      let errorMessage = "Failed to parse Excel file";
      if (err instanceof Error) {
        errorMessage = err.message;
        // Check for specific file errors
        if (err.message.includes("Failed to fetch") || err.message.includes("NetworkError")) {
          errorMessage = "Network error: Please check your internet connection and try again";
        } else if (err.message.includes("no sheets") || err.message.includes("empty")) {
          errorMessage = "Invalid file: The Excel file appears to be empty or corrupted";
        } else if (err.message.includes("Missing required columns")) {
          errorMessage = err.message; // Keep the specific column error
        }
      }
      setError(errorMessage);
      toast.error(errorMessage);
      setStep("upload");
      setFile(null);
    } finally {
      setIsLoading(false);
    }
  }, [loadSchools]);

  // Handle import confirmation
  const handleImport = useCallback(async () => {
    if (!file || !validationResult) return;

    const validRows = validationResult.validRows;
    if (validRows.length === 0) {
      toast.error("No valid rows to import");
      return;
    }

    setStep("importing");
    setIsLoading(true);
    setImportProgress({ current: 0, total: validRows.length, percentage: 0 });

    try {
      // Import with skipInvalidRows = true to skip invalid rows
      const results = await TeacherService.bulkImport(file, true);

      setImportResults(results);
      setImportProgress({ 
        current: results.successfulRows, 
        total: results.totalRows, 
        percentage: results.totalRows > 0 ? Math.round((results.successfulRows / results.totalRows) * 100) : 0 
      });
      setStep("results");

      if (results.successfulRows > 0) {
        toast.success(`Successfully imported ${results.successfulRows} teachers`);
        // Call the callback immediately after successful import to refresh the table
        onImportComplete?.();
      }
      if (results.failedRows > 0) {
        toast.error(`${results.failedRows} teachers failed to import`);
      }
    } catch (err) {
      let errorMessage = "Failed to import teachers";
      if (err instanceof Error) {
        errorMessage = err.message;
        // Check for network errors
        if (err.message.includes("Failed to fetch") || err.message.includes("NetworkError")) {
          errorMessage = "Network error: Please check your internet connection and try again";
        } else if (err.message.includes("timeout") || err.message.includes("Timeout")) {
          errorMessage = "Request timeout: The import is taking too long. Please try with a smaller file or contact support";
        } else if (err.message.includes("401") || err.message.includes("Unauthorized")) {
          errorMessage = "Authentication error: Please log in again";
        } else if (err.message.includes("403") || err.message.includes("Forbidden")) {
          errorMessage = "Permission denied: You don't have permission to import teachers";
        } else if (err.message.includes("413") || err.message.includes("too large")) {
          errorMessage = "File too large: Please use a smaller file (max 10MB)";
        }
      }
      setError(errorMessage);
      toast.error(errorMessage);
      setStep("preview");
    } finally {
      setIsLoading(false);
    }
  }, [file, validationResult, onImportComplete]);

  // Reset to start over
  const reset = useCallback(() => {
    setStep("upload");
    setFile(null);
    setParsedData([]);
    setValidationResult(null);
    setImportProgress({ current: 0, total: 0, percentage: 0 });
    setImportResults(null);
    setError(null);
    setIsLoading(false);
  }, []);

  // Start new import
  const startNewImport = useCallback(() => {
    reset();
  }, [reset]);

  return {
    step,
    file,
    parsedData,
    validationResult,
    importProgress,
    importResults,
    error,
    isLoading,
    handleFileSelect,
    handleImport,
    reset,
    startNewImport,
  };
}

