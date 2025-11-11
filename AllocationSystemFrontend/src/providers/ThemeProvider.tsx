import { useEffect, useState } from "react";
import { ThemeProviderContext } from "./theme-context";
import type { Theme, ThemeProviderProps, ResolvedTheme } from "@/types/theme.types";
import { applyTheme, getResolvedTheme, getSystemTheme } from "@/lib/theme";
import { useAppStore } from "@/store";

export function ThemeProvider({
  children,
  defaultTheme = "system",
  ...props
}: ThemeProviderProps) {
  const theme = useAppStore((s) => s.theme);
  const setTheme = useAppStore((s) => s.setTheme);

  // Set the theme to the default theme if it is not set
  useEffect(() => {
    if (!theme) {
      setTheme(defaultTheme as Theme);
    }
  }, [theme, setTheme, defaultTheme]);

  // Keep a reactive resolvedTheme so UI (icons) update on OS theme changes
  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>(
    () => getResolvedTheme(theme ?? defaultTheme)
  );

  // Recompute resolved theme when theme/default changes
  useEffect(() => {
    setResolvedTheme(getResolvedTheme(theme ?? defaultTheme));
  }, [theme, defaultTheme]);

  useEffect(() => {
    applyTheme(resolvedTheme);
  }, [resolvedTheme]);

  // Listen to system theme changes when theme is set to "system"
  useEffect(() => {
    if ((theme ?? defaultTheme) !== "system") return;
    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    const handleChange = () => {
      const next = getSystemTheme();
      applyTheme(next);
      setResolvedTheme(next);
    };
    mediaQuery.addEventListener("change", handleChange);
    return () => mediaQuery.removeEventListener("change", handleChange);
  }, [theme, defaultTheme]);

  return (
    <ThemeProviderContext.Provider
      {...props}
      value={{ theme: theme ?? defaultTheme, setTheme, resolvedTheme }}
    >
      {children}
    </ThemeProviderContext.Provider>
  );
}
