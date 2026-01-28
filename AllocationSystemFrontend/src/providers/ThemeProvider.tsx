import { useEffect, useState } from "react";
import { ThemeProviderContext } from "./theme-context";
import type { Theme, ThemeProviderProps, ResolvedTheme } from "@/types/theme.types";
import { applyTheme, getResolvedTheme, getSystemTheme } from "@/lib/theme";
import { useAppStore } from "@/store";

/**
 * Hook to manage theme initialization
 */
const useThemeInitialization = (defaultTheme: Theme) => {
  const theme = useAppStore(s => s.theme);
  const setTheme = useAppStore(s => s.setTheme);

  useEffect(() => {
    if (!theme) {
      setTheme(defaultTheme as Theme);
    }
  }, [theme, setTheme, defaultTheme]);

  return { theme, setTheme };
};

/**
 * Hook to manage resolved theme state
 */
const useResolvedTheme = (theme: Theme | undefined, defaultTheme: Theme) => {
  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>(
    () => getResolvedTheme(theme ?? defaultTheme)
  );

  useEffect(() => {
    setResolvedTheme(getResolvedTheme(theme ?? defaultTheme));
  }, [theme, defaultTheme]);

  useEffect(() => {
    applyTheme(resolvedTheme);
  }, [resolvedTheme]);

  return { resolvedTheme, setResolvedTheme };
};

/**
 * Hook to handle system theme changes
 */
const useSystemThemeListener = (
  theme: Theme | undefined,
  defaultTheme: Theme,
  setResolvedTheme: (theme: ResolvedTheme) => void
) => {
  useEffect(() => {
    const currentTheme = theme ?? defaultTheme;
    if (currentTheme !== "system") {
      return;
    }

    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    
    const handleChange = () => {
      const next = getSystemTheme();
      applyTheme(next);
      setResolvedTheme(next);
    };

    mediaQuery.addEventListener("change", handleChange);
    
    return () => {
      mediaQuery.removeEventListener("change", handleChange);
    };
  }, [theme, defaultTheme, setResolvedTheme]);
};

export function ThemeProvider({
  children,
  defaultTheme = "system",
  ...props
}: ThemeProviderProps) {
  const { theme, setTheme } = useThemeInitialization(defaultTheme);
  const { resolvedTheme, setResolvedTheme } = useResolvedTheme(theme, defaultTheme);
  
  useSystemThemeListener(theme, defaultTheme, setResolvedTheme);

  return (
    <ThemeProviderContext.Provider
      {...props}
      value={{ theme: theme ?? defaultTheme, setTheme, resolvedTheme }}
    >
      {children}
    </ThemeProviderContext.Provider>
  );
}