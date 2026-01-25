import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import "@testing-library/jest-dom/vitest";
import { render, screen, act } from "@testing-library/react";
import { ThemeProvider } from "../ThemeProvider";
import { ThemeProviderContext } from "../theme-context";
import { useContext } from "react";
import * as themeLib from "@/lib/theme";
import type { Theme } from "@/types/theme.types";
// Mock dependencies
vi.mock("@/store", () => ({
    useAppStore: vi.fn(),
}));

vi.mock("@/lib/theme", () => ({
    applyTheme: vi.fn(),
    getResolvedTheme: vi.fn(),
    getSystemTheme: vi.fn(),
}));

import { useAppStore } from "@/store";

describe("ThemeProvider", () => {
    const mockSetTheme = vi.fn<(theme: Theme) => void>();
    let matchMediaMock: ReturnType<typeof vi.fn>;
    let mediaQueryListeners: Record<string, EventListener> = {};

    beforeEach(() => {
        vi.clearAllMocks();
        mediaQueryListeners = {};

        // Mock window.matchMedia
        matchMediaMock = vi.fn().mockImplementation((query) => ({
            matches: false,
            media: query,
            onchange: null,
            addListener: vi.fn(),
            removeListener: vi.fn(),
            addEventListener: vi.fn((event, handler) => {
                if (event === 'change') {
                    mediaQueryListeners[event] = handler as EventListener;
                }
            }),
            removeEventListener: vi.fn(),
            dispatchEvent: vi.fn(),
        }));
        window.matchMedia = matchMediaMock as unknown as Window['matchMedia'];

        // Default store mock
        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: "system",
                setTheme: mockSetTheme,
            };
            return selector(state);
        });

        // Default theme lib mocks
        vi.mocked(themeLib.getResolvedTheme).mockReturnValue("light");
        vi.mocked(themeLib.getSystemTheme).mockReturnValue("light");
    });

    afterEach(() => {
        vi.resetAllMocks();
    });

    it("renders children", () => {
        render(
            <ThemeProvider>
                <div data-testid="child">Child Content</div>
            </ThemeProvider>
        );
        expect(screen.getByTestId("child")).toBeInTheDocument();
    });

    it("sets default theme if store theme is undefined", () => {
        // Mock store returning undefined theme
        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: undefined,
                setTheme: mockSetTheme,
            };
            return selector(state);
        });

        render(<ThemeProvider defaultTheme="dark">{null}</ThemeProvider>);

        expect(mockSetTheme).toHaveBeenCalledWith("dark");
    });

    it("applies the resolved theme on mount", () => {
        vi.mocked(themeLib.getResolvedTheme).mockReturnValue("dark");

        render(<ThemeProvider>{null}</ThemeProvider>);

        expect(themeLib.getResolvedTheme).toHaveBeenCalled();
        expect(themeLib.applyTheme).toHaveBeenCalledWith("dark");
    });

    it("updates resolved theme when theme changes", () => {
        // Initial render with light
        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: "light",
                setTheme: mockSetTheme,
            };
            return selector(state);
        });
        vi.mocked(themeLib.getResolvedTheme).mockReturnValue("light");

        const { rerender } = render(<ThemeProvider>{null}</ThemeProvider>);
        expect(themeLib.applyTheme).toHaveBeenCalledWith("light");

        // Re-render with dark
        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: "dark",
                setTheme: mockSetTheme,
            };
            return selector(state);
        });
        vi.mocked(themeLib.getResolvedTheme).mockReturnValue("dark");

        rerender(<ThemeProvider>{null}</ThemeProvider>);

        expect(themeLib.applyTheme).toHaveBeenCalledWith("dark");
    });

    it("listens to system theme changes when theme is system", () => {
        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: "system",
                setTheme: mockSetTheme,
            };
            return selector(state);
        });

        vi.mocked(themeLib.getSystemTheme).mockReturnValue("dark");

        render(<ThemeProvider>{null}</ThemeProvider>);

        // Check if event listener was added
        expect(matchMediaMock).toHaveBeenCalledWith("(prefers-color-scheme: dark)");

        // Simulate system change
        act(() => {
            if (mediaQueryListeners["change"]) {
                mediaQueryListeners["change"]({ matches: true } as MediaQueryListEvent);
            }
        });

        expect(themeLib.getSystemTheme).toHaveBeenCalled();
        expect(themeLib.applyTheme).toHaveBeenCalledWith("dark");
    });

    it("does not listen to system theme changes when theme is not system", () => {
        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: "light",
                setTheme: mockSetTheme,
            };
            return selector(state);
        });

        render(<ThemeProvider>{null}</ThemeProvider>);

        expect(matchMediaMock).not.toHaveBeenCalled();
    });

    it("provides theme context to children", () => {
        const TestComponent = () => {
            const context = useContext(ThemeProviderContext);
            return (
                <div>
                    <span data-testid="theme-value">{context.theme}</span>
                    <span data-testid="resolved-value">{context.resolvedTheme}</span>
                </div>
            );
        };

        (useAppStore as unknown as ReturnType<typeof vi.fn>).mockImplementation((selector: (state: unknown) => unknown) => {
            const state = {
                theme: "dark",
                setTheme: mockSetTheme,
            };
            return selector(state);
        });
        vi.mocked(themeLib.getResolvedTheme).mockReturnValue("dark");

        render(
            <ThemeProvider>
                <TestComponent />
            </ThemeProvider>
        );

        expect(screen.getByTestId("theme-value")).toHaveTextContent("dark");
        expect(screen.getByTestId("resolved-value")).toHaveTextContent("dark");
    });
});
