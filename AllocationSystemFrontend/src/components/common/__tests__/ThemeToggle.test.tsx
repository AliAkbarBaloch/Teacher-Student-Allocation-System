import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { ThemeToggle } from "@/components/common/ThemeToggle";
import type { ThemeProviderState, Theme } from "@/types/theme.types";

vi.mock("@/hooks/useTheme", () => ({
  useTheme: vi.fn(),
}));

import { useTheme } from "@/hooks/useTheme";

const mockedUseTheme = vi.mocked(useTheme);

const createThemeState = (theme: Theme, resolvedTheme: ThemeProviderState["resolvedTheme"]): ThemeProviderState & {
  setTheme: (theme: Theme) => void;
} => ({
  theme,
  resolvedTheme,
  setTheme: vi.fn(),
});

describe("ThemeToggle", () => {
  beforeEach(() => {
    mockedUseTheme.mockReset();
  });

  it("renders and shows current resolved theme in icon states", () => {
    const state = createThemeState("light", "light");
    mockedUseTheme.mockReturnValue(state);

    render(<ThemeToggle />);

    // The sun icon should be visible when resolvedTheme is light
    const button = screen.getByRole("button", { name: /toggle theme/i });
    expect(button).toBeInTheDocument();
  });

  it("calls setTheme with 'light', 'dark', and 'system' when menu items are clicked", async () => {
    const user = userEvent.setup();
    const state = createThemeState("system", "light");
    mockedUseTheme.mockReturnValue(state);

    render(<ThemeToggle />);

    const trigger = screen.getByRole("button", { name: /toggle theme/i });
    await user.click(trigger);

    await user.click(screen.getByText("Light"));
    expect(state.setTheme).toHaveBeenCalledWith("light");

    await user.click(trigger);
    await user.click(screen.getByText("Dark"));
    expect(state.setTheme).toHaveBeenCalledWith("dark");

    await user.click(trigger);
    await user.click(screen.getByText("System"));
    expect(state.setTheme).toHaveBeenCalledWith("system");
  });
});

