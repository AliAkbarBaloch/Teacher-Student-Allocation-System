/**
 * Downloads a blob as a file
 * @param blob - Blob to download
 * @param filename - Name of the file to download
 */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.style.display = "none"; // Hide the element
  document.body.appendChild(a);
  a.click();
  
  // Cleanup - use setTimeout to ensure click completes
  setTimeout(() => {
    window.URL.revokeObjectURL(url);
    if (a.parentNode) {
      document.body.removeChild(a);
    }
  }, 0);
}

/**
 * Generates a filename with current date
 * @param prefix - Filename prefix
 * @param extension - File extension (default: "csv")
 * @returns Generated filename
 */
export function generateFilename(prefix: string, extension = "csv"): string {
  const date = new Date().toISOString().split("T")[0];
  return `${prefix}-${date}.${extension}`;
}

