import { cn } from "@/lib/utils";
import { type ReactNode } from "react";

/**
 * Container Props
 */
interface ContainerProps {
  children: ReactNode;
  className?: string;
  as?: React.ElementType;
}

export function Container({ children, className, as: Component = "div" }: ContainerProps) {
  return (
    <Component
      className={cn(
        "w-full mx-auto px-4 sm:px-6 lg:px-8",
        "max-w-7xl", // Consistent max-width (1280px)
        className
      )}
    >
      {children}
    </Component>
  );
}

