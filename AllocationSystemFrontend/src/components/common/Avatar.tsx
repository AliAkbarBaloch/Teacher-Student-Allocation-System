import { cn } from "@/lib/utils";
import { Camera } from "lucide-react";
import { useRef } from "react";
import { useTranslation } from "react-i18next";

interface AvatarProps {
  name?: string;
  email?: string;
  size?: "sm" | "md" | "lg" | "xl";
  className?: string;
  showEditButton?: boolean;
  onEditClick?: () => void;
  onImageChange?: (file: File) => void;
  onError?: (error: string) => void;
}

export function Avatar({
  name,
  email,
  size = "md",
  className,
  showEditButton = false,
  onEditClick,
  onImageChange,
  onError,
}: AvatarProps) {
  const { t } = useTranslation("settings");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const getUserInitials = (name: string | undefined, email: string | undefined) => {
    if (name) {
      return name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .toUpperCase()
        .slice(0, 2);
    }
    if (email) {
      return email[0].toUpperCase();
    }
    return "U";
  };

  const sizeClasses = {
    sm: "h-16 w-16 text-lg",
    md: "h-24 w-24 text-2xl",
    lg: "h-32 w-32 text-3xl",
    xl: "h-40 w-40 text-4xl",
  };

  const buttonSizeClasses = {
    sm: "h-6 w-6",
    md: "h-8 w-8",
    lg: "h-10 w-10",
    xl: "h-12 w-12",
  };

  const initials = getUserInitials(name, email);

  const handleAvatarClick = () => {
    if (onEditClick) {
      onEditClick();
    } else if (onImageChange) {
      fileInputRef.current?.click();
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Validate file type
      if (!file.type.startsWith("image/")) {
        const errorMessage = t("profile.avatarError.invalidFileType");
        if (onError) {
          onError(errorMessage);
        }
        // Reset input so same file can be selected again
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
        return;
      }
      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        const errorMessage = t("profile.avatarError.fileTooLarge");
        if (onError) {
          onError(errorMessage);
        }
        // Reset input so same file can be selected again
        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }
        return;
      }
      if (onImageChange) {
        onImageChange(file);
      }
    }
    // Reset input so same file can be selected again
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  return (
    <div className={cn("relative inline-block", className)}>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileChange}
        className="hidden"
        aria-label="Upload avatar"
      />
      <div
        className={cn(
          "rounded-full bg-primary flex items-center justify-center text-primary-foreground font-semibold shadow-lg",
          "ring-4 ring-background",
          sizeClasses[size],
          "transition-transform duration-200 hover:scale-105"
        )}
      >
        {initials}
      </div>
      {showEditButton && (
        <button
          type="button"
          onClick={handleAvatarClick}
          className={cn(
            "absolute bottom-0 right-0 rounded-full bg-primary text-primary-foreground",
            "flex items-center justify-center shadow-lg",
            "hover:bg-primary/90 transition-colors duration-200",
            "border-2 border-background cursor-pointer",
            "active:scale-95",
            buttonSizeClasses[size]
          )}
          aria-label="Change avatar"
        >
          <Camera className="h-3 w-3" />
        </button>
      )}
    </div>
  );
}

