import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { renderHook, act } from "@testing-library/react";
import { useDebounce } from "../useDebounce";

describe("useDebounce", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it("should return initial value immediately", () => {
    const { result } = renderHook(() => useDebounce("test", 300));
    expect(result.current).toBe("test");
  });

  it("should debounce value changes", () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      {
        initialProps: { value: "initial", delay: 300 },
      }
    );

    expect(result.current).toBe("initial");

    // Change value
    act(() => {
      rerender({ value: "updated", delay: 300 });
    });

    // Value should not change immediately
    expect(result.current).toBe("initial");

    // Fast-forward time
    act(() => {
      vi.advanceTimersByTime(300);
    });

    // Value should be updated after delay
    expect(result.current).toBe("updated");
  });

  it("should cancel previous debounce on rapid changes", () => {
    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 300),
      {
        initialProps: { value: "initial" },
      }
    );

    // Rapid changes - each rerender resets the timer
    act(() => {
      rerender({ value: "change1" });
    });
    act(() => {
      vi.advanceTimersByTime(100);
    });
    
    act(() => {
      rerender({ value: "change2" });
    });
    act(() => {
      vi.advanceTimersByTime(100);
    });
    
    act(() => {
      rerender({ value: "change3" });
    });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    // Should still be initial (not enough time has passed)
    expect(result.current).toBe("initial");

    // Complete the delay from the last change
    act(() => {
      vi.advanceTimersByTime(200);
    });

    // Should only have the last value
    expect(result.current).toBe("change3");
  });

  it("should use custom delay", () => {
    const { result, rerender } = renderHook(
      ({ value, delay }) => useDebounce(value, delay),
      {
        initialProps: { value: "initial", delay: 500 },
      }
    );

    act(() => {
      rerender({ value: "updated", delay: 500 });
    });

    act(() => {
      vi.advanceTimersByTime(300);
    });
    expect(result.current).toBe("initial");

    act(() => {
      vi.advanceTimersByTime(200);
    });
    expect(result.current).toBe("updated");
  });
});

