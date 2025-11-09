/**
 * Theme type definitions
 * 
 * These types are used across the theme system (lib, providers, hooks, components)
 */

/**
 * Available theme modes
 */
export type Theme = "light" | "dark" | "system"

/**
 * Resolved theme (actual theme applied, excluding "system")
 */
export type ResolvedTheme = "light" | "dark"

/**
 * Theme provider state interface
 */
export interface ThemeProviderState {
  theme: Theme
  setTheme: (theme: Theme) => void
  resolvedTheme: ResolvedTheme
}

/**
 * Theme provider props
 */
export interface ThemeProviderProps {
  children: React.ReactNode
  defaultTheme?: Theme
  storageKey?: string
}

