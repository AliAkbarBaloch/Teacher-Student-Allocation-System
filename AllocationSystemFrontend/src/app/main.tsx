import '../lib/i18n'; // run i18n setup once at startup

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes";
import { ThemeProvider } from "../providers/ThemeProvider";

// Suppress harmless browser extension errors in console
if (typeof window !== "undefined") {
  const originalError = console.error;
  console.error = (...args: unknown[]) => {
    const errorMessage = args[0]?.toString() || "";
    // Filter out common browser extension errors
    if (
      errorMessage.includes("A listener indicated an asynchronous response") ||
      errorMessage.includes("message channel closed") ||
      errorMessage.includes("Extension context invalidated")
    ) {
      return; // Suppress these errors
    }
    originalError.apply(console, args);
  };
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ThemeProvider>
      <RouterProvider router={router} />
    </ThemeProvider>
  </StrictMode>
);
