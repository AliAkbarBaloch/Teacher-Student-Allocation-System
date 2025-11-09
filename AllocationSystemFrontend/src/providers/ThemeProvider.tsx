import { useThemeMode } from "@/lib/theme";
import { ThemeProviderContext } from "./theme-context";
import type { ThemeProviderProps } from "@/types/theme.types";

export function ThemeProvider({
  children,
  defaultTheme = "system",
  storageKey = "vite-ui-theme",
  ...props
}: ThemeProviderProps) {
  const themeState = useThemeMode(defaultTheme, storageKey);

  return (
    <ThemeProviderContext.Provider {...props} value={themeState}>
      {children}
    </ThemeProviderContext.Provider>
  );
}
