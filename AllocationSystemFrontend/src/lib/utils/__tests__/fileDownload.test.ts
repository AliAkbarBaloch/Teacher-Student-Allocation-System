import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { downloadBlob, generateFilename } from "../fileDownload";

describe("generateFilename", () => {
  it("should generate filename with prefix and date", () => {
    const filename = generateFilename("test-file");
    expect(filename).toMatch(/^test-file-\d{4}-\d{2}-\d{2}\.csv$/);
  });

  it("should use custom extension", () => {
    const filename = generateFilename("test-file", "json");
    expect(filename).toMatch(/^test-file-\d{4}-\d{2}-\d{2}\.json$/);
  });

  it("should use default csv extension", () => {
    const filename = generateFilename("test-file");
    expect(filename.endsWith(".csv")).toBe(true);
  });
});

describe("downloadBlob", () => {
  let createObjectURLSpy: ReturnType<typeof vi.spyOn>;
  let revokeObjectURLSpy: ReturnType<typeof vi.spyOn>;
  let createElementSpy: ReturnType<typeof vi.spyOn>;
  let appendChildSpy: ReturnType<typeof vi.spyOn>;
  let removeChildSpy: ReturnType<typeof vi.spyOn>;
  let mockClick: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    createObjectURLSpy = vi.spyOn(window.URL, "createObjectURL").mockReturnValue("blob:url");
    revokeObjectURLSpy = vi.spyOn(window.URL, "revokeObjectURL");
    mockClick = vi.fn();
    
    // Mock anchor element with style property
    const mockAnchor = {
      href: "",
      download: "",
      click: mockClick,
      style: {
        display: "",
      },
      parentNode: document.body,
    } as unknown as HTMLAnchorElement;

    createElementSpy = vi.spyOn(document, "createElement").mockReturnValue(mockAnchor);
    appendChildSpy = vi.spyOn(document.body, "appendChild").mockReturnValue(mockAnchor);
    removeChildSpy = vi.spyOn(document.body, "removeChild").mockReturnValue(mockAnchor);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("should create blob URL and trigger download", async () => {
    vi.useFakeTimers();
    const blob = new Blob(["test"], { type: "text/csv" });
    const filename = "test.csv";

    downloadBlob(blob, filename);

    expect(createObjectURLSpy).toHaveBeenCalledWith(blob);
    expect(createElementSpy).toHaveBeenCalledWith("a");
    expect(appendChildSpy).toHaveBeenCalled();
    expect(mockClick).toHaveBeenCalled();
    
    // Fast-forward to trigger cleanup
    vi.advanceTimersByTime(1);
    
    expect(removeChildSpy).toHaveBeenCalled();
    expect(revokeObjectURLSpy).toHaveBeenCalledWith("blob:url");
    
    vi.useRealTimers();
  });
});

