import { useState, type FormEvent, useEffect, useRef, useMemo } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useTranslation } from "react-i18next";
import { cn } from "@/lib/utils";
import { Eye, EyeOff, AlertCircle, CheckCircle2, Check, X } from "lucide-react";

interface PasswordStrength {
  score: number; // 0-4
  label: string;
  color: string;
  checks: {
    minLength: boolean;
    hasUppercase: boolean;
    hasLowercase: boolean;
    hasNumber: boolean;
    hasSpecial: boolean;
  };
}

function calculatePasswordStrength(password: string): PasswordStrength {
  const checks = {
    minLength: password.length >= 8,
    hasUppercase: /[A-Z]/.test(password),
    hasLowercase: /[a-z]/.test(password),
    hasNumber: /[0-9]/.test(password),
    hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(password),
  };

  const score = Object.values(checks).filter(Boolean).length;
  
  let label = "";
  let color = "";
  
  if (score <= 1) {
    label = "weak";
    color = "text-red-500";
  } else if (score === 2) {
    label = "fair";
    color = "text-orange-500";
  } else if (score === 3) {
    label = "good";
    color = "text-yellow-500";
  } else {
    label = "strong";
    color = "text-green-500";
  }

  return { score, label, color, checks };
}

export function SettingsPasswordForm() {
  const { t } = useTranslation("settings");
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{
    oldPassword?: string;
    newPassword?: string;
    confirmPassword?: string;
  }>({});
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [debouncedPassword, setDebouncedPassword] = useState("");

  // Debounce password strength calculation
  useEffect(() => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    debounceRef.current = setTimeout(() => {
      setDebouncedPassword(newPassword);
    }, 300);

    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [newPassword]);

  const passwordStrength = useMemo(
    () => calculatePasswordStrength(debouncedPassword),
    [debouncedPassword]
  );

  // Cleanup timeouts on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  const validateForm = (): boolean => {
    const errors: { oldPassword?: string; newPassword?: string; confirmPassword?: string } = {};

    if (!oldPassword.trim()) {
      errors.oldPassword = t("password.oldPasswordRequired");
    }

    if (!newPassword.trim()) {
      errors.newPassword = t("password.newPasswordRequired");
    } else {
      // Check if new password is same as old password (only if old password is provided)
      if (oldPassword.trim() && newPassword.trim() === oldPassword.trim()) {
        errors.newPassword = t("password.passwordSameAsOld");
      } else if (passwordStrength.score < 2) {
        errors.newPassword = t("password.passwordTooWeak");
      }
    }

    if (!confirmPassword.trim()) {
      errors.confirmPassword = t("password.confirmPasswordRequired");
    } else if (newPassword.trim() !== confirmPassword.trim()) {
      errors.confirmPassword = t("password.passwordsDoNotMatch");
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setIsSuccess(false);
    setFieldErrors({});

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      // Simulate API call - in real app, this would call the backend
      await new Promise((resolve) => setTimeout(resolve, 1500));

      // Mock old password validation
      // In real app, backend would return 400 if old password is incorrect
      // TODO: Replace with actual API call to verify old password
      const mockCurrentPassword = "password"; // This is just for development/demo
      if (oldPassword.trim() !== mockCurrentPassword) {
        throw new Error(t("password.incorrectOldPassword"));
      }

      setIsSuccess(true);
      setOldPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setDebouncedPassword("");
      // Clear existing timeout
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
      timeoutRef.current = setTimeout(() => setIsSuccess(false), 3000);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : "An error occurred";
      
      // Check if it's an old password error
      if (errorMessage.includes("old password") || errorMessage.includes("incorrect")) {
        setFieldErrors((prev) => ({ ...prev, oldPassword: errorMessage }));
      } else {
        setError(errorMessage);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (
    field: "oldPassword" | "newPassword" | "confirmPassword",
    value: string
  ) => {
    if (field === "oldPassword") {
      setOldPassword(value);
    } else if (field === "newPassword") {
      setNewPassword(value);
    } else {
      setConfirmPassword(value);
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
          <p className="font-medium flex-1">{t("password.changeSuccess")}</p>
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
        {/* Old Password Field */}
        <div className="space-y-2">
          <Label htmlFor="oldPassword" className="text-sm font-medium">
            {t("password.oldPassword")}
          </Label>
          <div className="relative">
          <Input
            id="oldPassword"
            type={showOldPassword ? "text" : "password"}
            value={oldPassword}
            onChange={(e) => handleInputChange("oldPassword", e.target.value)}
            placeholder={t("password.oldPasswordPlaceholder")}
            className={cn(
              "pr-10 transition-all duration-200",
              fieldErrors.oldPassword && "border-destructive focus-visible:ring-destructive"
            )}
          />
          <button
            type="button"
            onClick={() => setShowOldPassword(!showOldPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            aria-label={showOldPassword ? t("password.hidePassword") : t("password.showPassword")}
          >
            {showOldPassword ? (
              <EyeOff className="h-4 w-4" aria-hidden="true" />
            ) : (
              <Eye className="h-4 w-4" aria-hidden="true" />
            )}
          </button>
          </div>
          {fieldErrors.oldPassword && (
            <p className="text-sm text-destructive flex items-center gap-1.5 animate-in fade-in slide-in-from-top-1">
              <AlertCircle className="h-3.5 w-3.5" />
              {fieldErrors.oldPassword}
            </p>
          )}
        </div>

      {/* New Password Field */}
      <div className="space-y-2">
        <Label htmlFor="newPassword" className="text-sm font-medium">
          {t("password.newPassword")}
        </Label>
        <div className="relative">
          <Input
            id="newPassword"
            type={showNewPassword ? "text" : "password"}
            value={newPassword}
            onChange={(e) => handleInputChange("newPassword", e.target.value)}
            placeholder={t("password.newPasswordPlaceholder")}
            className={cn(
              "pr-10 transition-all duration-200",
              fieldErrors.newPassword && "border-destructive focus-visible:ring-destructive"
            )}
          />
          <button
            type="button"
            onClick={() => setShowNewPassword(!showNewPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            aria-label={showNewPassword ? t("password.hidePassword") : t("password.showPassword")}
          >
            {showNewPassword ? (
              <EyeOff className="h-4 w-4" aria-hidden="true" />
            ) : (
              <Eye className="h-4 w-4" aria-hidden="true" />
            )}
          </button>
        </div>
        {fieldErrors.newPassword && (
          <p className="text-sm text-destructive flex items-center gap-1.5 animate-in fade-in slide-in-from-top-1">
            <AlertCircle className="h-3.5 w-3.5" />
            {fieldErrors.newPassword}
          </p>
        )}

        {/* Password Strength Indicator */}
        {newPassword && (
          <div className="space-y-3 pt-3 mt-3 border-t animate-in fade-in slide-in-from-top-2 duration-300">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">{t("password.passwordStrength")}</span>
              <span className={cn("font-medium", passwordStrength.color)}>
                {t(`password.strength.${passwordStrength.label}`)}
              </span>
            </div>
            <div className="h-2 bg-muted rounded-full overflow-hidden">
              <div
                className={cn(
                  "h-full transition-all duration-300",
                  passwordStrength.score <= 1 && "bg-red-500",
                  passwordStrength.score === 2 && "bg-orange-500",
                  passwordStrength.score === 3 && "bg-yellow-500",
                  passwordStrength.score >= 4 && "bg-green-500"
                )}
                style={{ width: `${(passwordStrength.score / 4) * 100}%` }}
              />
            </div>
            <div className="space-y-1.5 text-sm">
              <div className={cn("flex items-center gap-2", passwordStrength.checks.minLength ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                {passwordStrength.checks.minLength ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <X className="h-4 w-4" />
                )}
                <span>{t("password.strength.minLength")}</span>
              </div>
              <div className={cn("flex items-center gap-2", passwordStrength.checks.hasUppercase ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                {passwordStrength.checks.hasUppercase ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <X className="h-4 w-4" />
                )}
                <span>{t("password.strength.hasUppercase")}</span>
              </div>
              <div className={cn("flex items-center gap-2", passwordStrength.checks.hasLowercase ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                {passwordStrength.checks.hasLowercase ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <X className="h-4 w-4" />
                )}
                <span>{t("password.strength.hasLowercase")}</span>
              </div>
              <div className={cn("flex items-center gap-2", passwordStrength.checks.hasNumber ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                {passwordStrength.checks.hasNumber ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <X className="h-4 w-4" />
                )}
                <span>{t("password.strength.hasNumber")}</span>
              </div>
              <div className={cn("flex items-center gap-2", passwordStrength.checks.hasSpecial ? "text-green-600 dark:text-green-400" : "text-muted-foreground")}>
                {passwordStrength.checks.hasSpecial ? (
                  <Check className="h-4 w-4" />
                ) : (
                  <X className="h-4 w-4" />
                )}
                <span>{t("password.strength.hasSpecial")}</span>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Confirm Password Field */}
      <div className="space-y-2">
        <Label htmlFor="confirmPassword" className="text-sm font-medium">
          {t("password.confirmPassword")}
        </Label>
        <div className="relative">
          <Input
            id="confirmPassword"
            type={showConfirmPassword ? "text" : "password"}
            value={confirmPassword}
            onChange={(e) => handleInputChange("confirmPassword", e.target.value)}
            placeholder={t("password.confirmPasswordPlaceholder")}
            className={cn(
              "pr-10 transition-all duration-200",
              fieldErrors.confirmPassword && "border-destructive focus-visible:ring-destructive"
            )}
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
            aria-label={showConfirmPassword ? t("password.hidePassword") : t("password.showPassword")}
          >
            {showConfirmPassword ? (
              <EyeOff className="h-4 w-4" aria-hidden="true" />
            ) : (
              <Eye className="h-4 w-4" aria-hidden="true" />
            )}
          </button>
        </div>
        {fieldErrors.confirmPassword && (
          <p className="text-sm text-destructive flex items-center gap-1.5 animate-in fade-in slide-in-from-top-1">
            <AlertCircle className="h-3.5 w-3.5" />
            {fieldErrors.confirmPassword}
          </p>
        )}
        </div>

        {/* Submit Button */}
        <Button type="submit" disabled={isLoading} className="min-w-[140px]">
          {isLoading ? t("password.changing") : t("password.changeButton")}
        </Button>
      </div>
    </form>
  );
}

