import { useState, type FormEvent, useEffect, useRef, useMemo } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { AuthService } from "@/features/auth/services/authService";
import { cn } from "@/lib/utils";
import { validateEmail, validatePhoneNumber } from "@/lib/validationUtils";
import { AlertCircle, CheckCircle2, X } from "lucide-react";

export function ProfileForm() {
  const { t } = useTranslation("settings");
  const { user, updateProfile } = useAuth();
  
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [initialValues, setInitialValues] = useState({ fullName: "", email: "", phoneNumber: "" });
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{
    fullName?: string;
    email?: string;
    phoneNumber?: string;
  }>({});
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Load profile data on mount and sync form fields when user changes
  useEffect(() => {
    const loadProfile = async () => {
      if (!user) {
        // Reset form when user is null (logged out)
        setFullName("");
        setEmail("");
        setPhoneNumber("");
        setInitialValues({ fullName: "", email: "", phoneNumber: "" });
        setIsLoadingProfile(false);
        return;
      }

      setIsLoadingProfile(true);
      try {
        const profile = await AuthService.getProfile();
        const profileFullName = profile.fullName || "";
        const profileEmail = profile.email || "";
        const profilePhoneNumber = profile.phoneNumber || "";
        setFullName(profileFullName);
        setEmail(profileEmail);
        setPhoneNumber(profilePhoneNumber);
        setInitialValues({ fullName: profileFullName, email: profileEmail, phoneNumber: profilePhoneNumber });
      } catch (error) {
        console.error("Failed to load profile:", error);
        // Fallback to user from auth state
        const fallbackFullName = user.fullName || user.name || "";
        const fallbackEmail = user.email || "";
        const fallbackPhoneNumber = "";
        setFullName(fallbackFullName);
        setEmail(fallbackEmail);
        setPhoneNumber(fallbackPhoneNumber);
        setInitialValues({ fullName: fallbackFullName, email: fallbackEmail, phoneNumber: fallbackPhoneNumber });
      } finally {
        setIsLoadingProfile(false);
      }
    };

    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id, user?.email]); // Use specific user properties to detect changes

  // Check if form has changes
  const hasChanges = useMemo(() => {
    return fullName.trim() !== initialValues.fullName.trim() || 
           email.trim() !== initialValues.email.trim() ||
           phoneNumber.trim() !== (initialValues.phoneNumber || "").trim();
  }, [fullName, email, phoneNumber, initialValues]);

  const validateForm = (): boolean => {
    const errors: { fullName?: string; email?: string; phoneNumber?: string } = {};

    if (!fullName.trim()) {
      errors.fullName = t("profile.fullNameRequired");
    }

    if (!email.trim()) {
      errors.email = t("profile.emailRequired");
    } else if (!validateEmail(email)) {
      errors.email = t("profile.emailInvalid");
    }

    // Phone number is optional, but if provided, validate format
    if (phoneNumber.trim() && !validatePhoneNumber(phoneNumber)) {
      errors.phoneNumber = t("profile.phoneInvalid");
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsSuccess(false);
    setFieldErrors({});

    // Check if there are any changes
    if (!hasChanges) {
      setError(t("profile.noChanges"));
      return;
    }

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      const updatedProfile = await AuthService.updateProfile({
        email,
        fullName,
        phoneNumber: phoneNumber.trim() || undefined,
      });

      // Update local auth state (preserve role)
      updateProfile({ 
        name: updatedProfile.fullName.split(" ")[0] || updatedProfile.fullName,
        fullName: updatedProfile.fullName,
        email: updatedProfile.email 
      });

      // Update initial values to reflect the saved state
      setInitialValues({ 
        fullName: updatedProfile.fullName, 
        email: updatedProfile.email,
        phoneNumber: updatedProfile.phoneNumber || ""
      });

      setIsSuccess(true);
      // Clear existing timeout
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      timeoutRef.current = setTimeout(() => setIsSuccess(false), 3000);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "An error occurred";
      
      // Check if it's an email uniqueness error
      if (errorMessage.toLowerCase().includes("email") || 
          errorMessage.toLowerCase().includes("already in use")) {
        setFieldErrors((prev) => ({ ...prev, email: errorMessage }));
      } else {
        setError(errorMessage);
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  const handleInputChange = (field: "fullName" | "email" | "phoneNumber", value: string) => {
    if (field === "fullName") {
      setFullName(value);
    } else if (field === "email") {
      setEmail(value);
    } else {
      setPhoneNumber(value);
    }

    // Clear field error when user starts typing
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: undefined }));
    }
    if (error) {
      setError(null);
    }
    if (isSuccess) {
      setIsSuccess(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      {/* Success Message */}
      {isSuccess && (
        <div
          className={cn(
            "flex items-start gap-3 p-4 rounded-lg text-sm max-w-md",
            "bg-green-50 dark:bg-green-950/20 text-green-800 dark:text-green-200 border border-green-200 dark:border-green-800",
            "animate-in fade-in slide-in-from-top-2 duration-300"
          )}
        >
          <CheckCircle2 className="h-5 w-5 shrink-0 mt-0.5" />
          <p className="font-medium flex-1">{t("profile.updateSuccess")}</p>
          <button
            type="button"
            onClick={() => {
              setIsSuccess(false);
              if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
              }
            }}
            className="ml-2 hover:bg-green-100 dark:hover:bg-green-900/30 rounded p-0.5 transition-colors"
            aria-label="Dismiss"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* General Error */}
      {error && (
        <div
          className={cn(
            "flex items-start gap-3 p-4 rounded-lg text-sm max-w-md",
            "bg-destructive/10 text-destructive border border-destructive/20",
            "animate-in fade-in slide-in-from-top-2 duration-300"
          )}
        >
          <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
          <p className="flex-1">{error}</p>
          <button
            type="button"
            onClick={() => setError(null)}
            className="ml-2 hover:bg-destructive/20 rounded p-0.5 transition-colors"
            aria-label="Dismiss"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      <div className="space-y-5 max-w-lg">
        {/* Full Name Field */}
        <div className="space-y-2">
          <Label htmlFor="fullName" className="text-sm font-medium">
            {t("profile.fullName")}
          </Label>
          <Input
            id="fullName"
            type="text"
            value={fullName}
            onChange={(e) => handleInputChange("fullName", e.target.value)}
            placeholder={t("profile.fullNamePlaceholder")}
            disabled={isLoadingProfile || isLoading}
            className={cn(
              "transition-all duration-200",
              fieldErrors.fullName && "border-destructive focus-visible:ring-destructive"
            )}
          />
          {fieldErrors.fullName && (
            <p className="text-sm text-destructive flex items-center gap-1.5 animate-in fade-in slide-in-from-top-1">
              <AlertCircle className="h-3.5 w-3.5" />
              {fieldErrors.fullName}
            </p>
          )}
        </div>

        {/* Email Field */}
        <div className="space-y-2">
          <Label htmlFor="email" className="text-sm font-medium">
            {t("profile.email")}
          </Label>
          <Input
            id="email"
            type="email"
            value={email}
            onChange={(e) => handleInputChange("email", e.target.value)}
            placeholder={t("profile.emailPlaceholder")}
            disabled={isLoadingProfile || isLoading}
            className={cn(
              "transition-all duration-200",
              fieldErrors.email && "border-destructive focus-visible:ring-destructive"
            )}
          />
          {fieldErrors.email && (
            <p className="text-sm text-destructive flex items-center gap-1.5 animate-in fade-in slide-in-from-top-1">
              <AlertCircle className="h-3.5 w-3.5" />
              {fieldErrors.email}
            </p>
          )}
        </div>

        {/* Phone Number Field */}
        <div className="space-y-2">
          <Label htmlFor="phoneNumber" className="text-sm font-medium">
            {t("profile.phoneNumber")}
          </Label>
          <Input
            id="phoneNumber"
            type="tel"
            value={phoneNumber}
            onChange={(e) => handleInputChange("phoneNumber", e.target.value)}
            placeholder={t("profile.phoneNumberPlaceholder")}
            disabled={isLoadingProfile || isLoading}
            className={cn(
              "transition-all duration-200",
              fieldErrors.phoneNumber && "border-destructive focus-visible:ring-destructive"
            )}
          />
          {fieldErrors.phoneNumber && (
            <p className="text-sm text-destructive flex items-center gap-1.5 animate-in fade-in slide-in-from-top-1">
              <AlertCircle className="h-3.5 w-3.5" />
              {fieldErrors.phoneNumber}
            </p>
          )}
        </div>

        {/* Submit Button */}
        <Button type="submit" disabled={isLoadingProfile || isLoading || !hasChanges} className="min-w-[140px]">
          {isLoadingProfile ? t("profile.loading") : isLoading ? t("profile.updating") : t("profile.updateButton")}
        </Button>
      </div>
    </form>
  );
}

