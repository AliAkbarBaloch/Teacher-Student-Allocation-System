import '../lib/i18n'; // run i18n setup once at startup

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { Toaster } from "sonner";
import { router } from "./routes";
import { ThemeProvider } from "../providers/ThemeProvider";
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

const queryClient = new QueryClient();

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
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <RouterProvider router={router} />
        <Toaster position="bottom-right" richColors />
        <ReactQueryDevtools />
      </ThemeProvider>
    </QueryClientProvider>
  </StrictMode>
);
