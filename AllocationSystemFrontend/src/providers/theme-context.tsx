import { createContext } from "react";
import type { ThemeProviderState } from "@/types/theme.types";

const initialState: ThemeProviderState = {
  theme: "system",
  setTheme: () => null,
  resolvedTheme: "light",
};

export const ThemeProviderContext =
  createContext<ThemeProviderState>(initialState);

