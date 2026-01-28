import { useEffect, useState } from "react"
import type { Theme, ResolvedTheme } from "@/types/theme.types"

/**
 * Get the system theme preference
 */
export function getSystemTheme(): ResolvedTheme {
  if (typeof window === "undefined") {
    return "light";
  }
  if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
    return "dark";
  } else {
    return "light";
  }
}

/**
 * Get the resolved theme (actual theme to apply)
 */
export function getResolvedTheme(theme: Theme): ResolvedTheme {
  if (theme === "system") {
    return getSystemTheme()
  }
  return theme
}

/**
 * Apply theme classes to the document root
 */
export function applyTheme(theme: ResolvedTheme) {
  if (typeof window === "undefined") {
    return;
  }

  const root = window.document.documentElement
  if (theme === "dark") {
    root.classList.add("dark");
  } else {
    root.classList.remove("dark");
  }
}

/**
 * Get theme from localStorage
 */
export function getStoredTheme(
  storageKey: string,
  defaultTheme: Theme
): Theme {
  if (typeof window === "undefined") {
    return defaultTheme;
  }
  const stored = localStorage.getItem(storageKey) as Theme | null
  return stored || defaultTheme
}

/**
 * Save theme to localStorage
 */
export function saveTheme(storageKey: string, theme: Theme) {
  if (typeof window === "undefined") {
    return;
  }
  localStorage.setItem(storageKey, theme)
}

/**
 * Hook to manage theme mode with system preference detection
 */
export function useThemeMode(
  defaultTheme: Theme = "system",
  storageKey: string = "vite-ui-theme"
) {
  const [theme, setThemeState] = useState<Theme>(() =>
    getStoredTheme(storageKey, defaultTheme)
  )

  const [resolvedTheme, setResolvedTheme] = useState<ResolvedTheme>(() =>
    getResolvedTheme(getStoredTheme(storageKey, defaultTheme))
  )

  // Apply theme when it changes
  useEffect(() => {
    const resolved = getResolvedTheme(theme)
    applyTheme(resolved)
    setResolvedTheme(resolved)
  }, [theme])

  // Listen for system theme changes when theme is "system"
  useEffect(() => {
    if (theme !== "system") {
      return
    }

    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)")

    const handleChange = () => {
      const systemTheme = getSystemTheme()
      applyTheme(systemTheme)
      setResolvedTheme(systemTheme)
    }

    mediaQuery.addEventListener("change", handleChange)
    return () => mediaQuery.removeEventListener("change", handleChange)
  }, [theme])

  const setTheme = (newTheme: Theme) => {
    saveTheme(storageKey, newTheme)
    setThemeState(newTheme)
  }

  return {
    theme,
    setTheme,
    resolvedTheme,
  }
}

