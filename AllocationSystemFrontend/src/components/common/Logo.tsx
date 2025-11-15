import { GraduationCap } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { cn } from "@/lib/utils";

interface LogoProps {
  className?: string;
  showText?: boolean;
  size?: "sm" | "md" | "lg";
  linkTo?: string | null;
}

export function Logo({ className, showText = true, size = "md", linkTo }: LogoProps) {
  const { t } = useTranslation("common");
  const href = linkTo ?? ROUTES.main.home;

  const sizeClasses = {
    sm: {
      icon: "h-6 w-6",
      container: "h-8 w-8",
      text: "text-lg",
      gap: "gap-1.5",
    },
    md: {
      icon: "h-6 w-6",
      container: "h-10 w-10",
      text: "text-xl lg:text-2xl",
      gap: "gap-2",
    },
    lg: {
      icon: "h-8 w-8",
      container: "h-12 w-12",
      text: "text-2xl lg:text-3xl",
      gap: "gap-3",
    },
  };

  const classes = sizeClasses[size];

  const logoContent = (
    <div className={cn("flex items-center", classes.gap, className)}>
      <div
        className={cn(
          "rounded-lg flex items-center justify-center bg-primary transition-transform duration-200 hover:scale-105",
          classes.container
        )}
      >
        <GraduationCap className={cn("text-primary-foreground", classes.icon)} />
      </div>
      {showText && (
        <span className={cn("font-bold tracking-tight", classes.text)}>
          {t("app.title")}
        </span>
      )}
    </div>
  );

  if (linkTo !== null) {
    return (
      <Link to={href} className="inline-block">
        {logoContent}
      </Link>
    );
  }

  return logoContent;
}

