/**
 * Formats a date string for display
 * @param dateString - ISO date string
 * @param locale - Locale string (default: browser locale)
 * @returns Formatted date string or original string if invalid
 */
export function formatDate(dateString: string, locale?: string): string {
  try {
    const date = new Date(dateString);
    if (Number.isNaN(date.getTime())) {
      return dateString;
    }
    return date.toLocaleString(locale);
  } catch {
    return dateString;
  }
}

/**
 * Formats a date string for datetime-local input
 * @param dateString - ISO date string
 * @returns Formatted string (YYYY-MM-DDTHH:mm) or empty string
 */
export function formatDateForInput(dateString: string | null | undefined): string {
  if (!dateString) {
    return "";
  }
  try {
    const date = new Date(dateString);
    if (Number.isNaN(date.getTime())) {
      return "";
    }
    return date.toISOString().slice(0, 16);
  } catch {
    return "";
  }
}

/**
 * Parses a datetime-local input value to ISO string
 * @param inputValue - Value from datetime-local input
 * @returns ISO date string or undefined
 */
export function parseDateInput(inputValue: string | null | undefined): string | undefined {
  if (!inputValue) {
    return undefined;
  }
  try {
    const date = new Date(inputValue);
    if (Number.isNaN(date.getTime())) {
      return undefined;
    }
    return date.toISOString();
  } catch {
    return undefined;
  }
}

