import React, { Component, type ReactNode } from "react";
import { AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";

/**
 * Props for the ErrorBoundary component
 */
interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: React.ErrorInfo) => void;
}

/**
 * State for the ErrorBoundary internal class component
 */
interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

/**
 * Helper function to check if we're in development mode
 * Supports both Vite (import.meta.env) and Node (process.env)
 */
function isDevelopmentMode(): boolean {
  try {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const globalThisAny = globalThis as any;
    if (typeof import.meta !== "undefined" && import.meta.env) {
      return import.meta.env.MODE === "development";
    } else if (globalThisAny.process?.env) {
      return globalThisAny.process.env.NODE_ENV === "development";
    }
  } catch {
    // If neither is available, default to false
  }
  return false;
}

/**
 * Internal class component for error boundary functionality
 * React Error Boundaries can only be implemented as class components
 */
class ErrorBoundaryClass extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
    };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
    // Log error to console in development
    if (isDevelopmentMode()) {
      console.error("ErrorBoundary caught an error:", error, errorInfo);
    }

    // Call optional error handler
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
    });
  };

  /**
   * Renders the default fallback UI when no custom fallback prop is provided
   */
  private renderDefaultFallback(): ReactNode {
    const isDev = isDevelopmentMode();

    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] p-8">
        <div className="max-w-md w-full space-y-4">
          <div className="flex items-center gap-3 text-destructive">
            <AlertCircle className="h-6 w-6" aria-hidden="true" />
            <h2 className="text-xl font-semibold">Something went wrong</h2>
          </div>
          <p className="text-muted-foreground">
            An unexpected error occurred. Please try refreshing the page or
            contact support if the problem persists.
          </p>
          {isDev && this.state.error && (
            <details className="mt-4 p-4 bg-muted rounded-md">
              <summary className="cursor-pointer font-medium mb-2">
                Error Details (Development Only)
              </summary>
              <pre className="text-xs overflow-auto">
                {this.state.error.toString()}
                {this.state.error.stack && `\n\n${this.state.error.stack}`}
              </pre>
            </details>
          )}
          <div className="flex gap-2">
            <Button onClick={this.handleReset} variant="outline">
              Try Again
            </Button>
            <Button onClick={() => window.location.reload()} variant="default">
              Refresh Page
            </Button>
          </div>
        </div>
      </div>
    );
  }

  render(): ReactNode {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      return this.renderDefaultFallback();
    }

    return this.props.children;
  }
}

/**
 * Functional Error Boundary component
 * Wraps the class component to provide a functional API
 * 
 * @example
 * ```tsx
 * <ErrorBoundary onError={(error) => console.error(error)}>
 *   <MyComponent />
 * </ErrorBoundary>
 * ```
 */
export function ErrorBoundary({
  children,
  fallback,
  onError,
}: ErrorBoundaryProps): ReactNode {
  return (
    <ErrorBoundaryClass fallback={fallback} onError={onError}>
      {children}
    </ErrorBoundaryClass>
  );
}

