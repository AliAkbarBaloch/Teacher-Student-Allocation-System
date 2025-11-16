import { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { ProfileForm } from "@/features/settings/components/ProfileForm";
import { SettingsPasswordForm } from "@/features/settings/components/SettingsPasswordForm";
import { SettingsSidebar } from "@/features/settings/components/SettingsSidebar";
import { Avatar } from "@/components/common/Avatar";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { Link } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { Users, UserPlus, CheckCircle2, X, AlertCircle } from "lucide-react";
import { cn } from "@/lib/utils";
import type { SettingsSection } from "@/features/settings/types/settings.types";

export default function SettingsPage() {
  const { t } = useTranslation("settings");
  const { user, isAuthenticated } = useAuth();
  const [activeSection, setActiveSection] = useState<SettingsSection>("profile");
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarSuccess, setAvatarSuccess] = useState(false);
  const [avatarError, setAvatarError] = useState<string | null>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Check if user has admin role (backend returns "ADMIN" or "USER")
  const isAdmin = user?.role === "ADMIN";

  // Reset active section when user changes (e.g., after logout/login)
  useEffect(() => {
    if (!isAuthenticated || !user) {
      setActiveSection("profile");
    } else if (activeSection === "admin" && !isAdmin) {
      // If user is no longer admin, switch to profile
      setActiveSection("profile");
    }
  }, [user?.id, isAuthenticated, isAdmin, activeSection]);

  const handleAvatarChange = (file: File) => {
    setAvatarFile(file);
    setAvatarSuccess(true);
    setAvatarError(null);
    // TODO: Upload avatar to backend
    // For now, just show a success message
    // In real app, you would:
    // 1. Upload file to backend
    // 2. Get URL back
    // 3. Update user profile with avatar URL
    // 4. Update user state with new avatar URL
    
    // Clear existing timeout
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    
    // Hide success message after 3 seconds
    timeoutRef.current = setTimeout(() => {
      setAvatarSuccess(false);
    }, 3000);
  };

  const handleAvatarError = (error: string) => {
    setAvatarError(error);
    setAvatarSuccess(false);
    // Clear error after 5 seconds
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    timeoutRef.current = setTimeout(() => {
      setAvatarError(null);
    }, 5000);
  };

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">{t("title")}</h1>
        <p className="text-muted-foreground mt-2">
          {t("subtitle")}
        </p>
      </div>

      <div className="flex flex-col sm:flex-row gap-6 sm:gap-8">
        {/* Sidebar */}
        <SettingsSidebar
          activeSection={activeSection}
          onSectionChange={setActiveSection}
        />

        {/* Main Content */}
        <div className="flex-1 min-w-0 space-y-8">
          {/* Profile Section */}
          {activeSection === "profile" && (
            <div className="space-y-4">
              <div>
                <h2 className="text-2xl font-semibold tracking-tight">{t("profile.title")}</h2>
                <p className="text-muted-foreground text-sm mt-1">{t("profile.subtitle")}</p>
              </div>
              <div className="rounded-xl border bg-card shadow-sm overflow-hidden">
                {/* Avatar Section */}
                <div className="p-8 border-b bg-gradient-to-br from-muted/50 to-muted/20">
                  <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
                    <Avatar
                      key={user?.id || "no-user"}
                      name={user?.fullName || user?.name}
                      email={user?.email}
                      size="lg"
                      showEditButton
                      onImageChange={handleAvatarChange}
                      onError={handleAvatarError}
                    />
                    <div className="flex-1 min-w-0">
                      <h3 className="text-xl font-semibold">{user?.fullName || user?.name || "User"}</h3>
                      <p className="text-sm text-muted-foreground mt-1">{user?.email || ""}</p>
                      <p className="text-xs text-muted-foreground mt-3">
                        {t("profile.avatarHint")}
                      </p>
                      {avatarSuccess && (
                        <div
                          className={cn(
                            "flex items-center gap-2 mt-3 p-2 rounded-md text-xs max-w-md",
                            "bg-green-50 dark:bg-green-950/20 text-green-800 dark:text-green-200 border border-green-200 dark:border-green-800",
                            "animate-in fade-in slide-in-from-top-2 duration-300"
                          )}
                        >
                          <CheckCircle2 className="h-4 w-4 shrink-0" />
                          <span className="font-medium flex-1">
                            {avatarFile 
                              ? t("profile.avatarSelected", { fileName: avatarFile.name })
                              : t("profile.avatarSelectedGeneric")
                            }
                          </span>
                          <button
                            type="button"
                            onClick={() => {
                              setAvatarSuccess(false);
                              if (timeoutRef.current) {
                                clearTimeout(timeoutRef.current);
                              }
                            }}
                            className="ml-2 hover:bg-green-100 dark:hover:bg-green-900/30 rounded p-0.5 transition-colors"
                            aria-label="Dismiss"
                          >
                            <X className="h-3 w-3" />
                          </button>
                        </div>
                      )}
                      {avatarError && (
                        <div
                          className={cn(
                            "flex items-center gap-2 mt-3 p-2 rounded-md text-xs max-w-md",
                            "bg-destructive/10 text-destructive border border-destructive/20",
                            "animate-in fade-in slide-in-from-top-2 duration-300"
                          )}
                        >
                          <AlertCircle className="h-4 w-4 shrink-0" />
                          <span className="font-medium flex-1">{avatarError}</span>
                          <button
                            type="button"
                            onClick={() => {
                              setAvatarError(null);
                              if (timeoutRef.current) {
                                clearTimeout(timeoutRef.current);
                              }
                            }}
                            className="ml-2 hover:bg-destructive/20 rounded p-0.5 transition-colors"
                            aria-label="Dismiss"
                          >
                            <X className="h-3 w-3" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
                {/* Form Section */}
                <div className="p-8">
                  <ProfileForm />
                </div>
              </div>
            </div>
          )}

          {/* Password Section */}
          {activeSection === "password" && (
            <div className="space-y-4">
              <div>
                <h2 className="text-2xl font-semibold tracking-tight">{t("password.title")}</h2>
                <p className="text-muted-foreground text-sm mt-1">{t("password.subtitle")}</p>
              </div>
              <div className="rounded-xl border bg-card shadow-sm p-8">
                <SettingsPasswordForm />
              </div>
            </div>
          )}

          {/* Admin Section */}
          {activeSection === "admin" && (
            isAdmin ? (
              <div className="space-y-4">
                <div>
                  <h2 className="text-2xl font-semibold tracking-tight">{t("admin.title")}</h2>
                  <p className="text-muted-foreground text-sm mt-1">{t("admin.subtitle")}</p>
                </div>
                <div className="rounded-xl border bg-card shadow-sm p-6">
                  <nav className="space-y-2">
                    <Link
                      to={ROUTES.admin.users}
                      className={cn(
                        "flex items-center gap-3 px-4 py-3 rounded-lg",
                        "transition-all duration-200",
                        "hover:bg-accent hover:text-accent-foreground",
                        "text-sm font-medium"
                      )}
                    >
                      <Users className="h-4 w-4" />
                      <span>{t("admin.viewUsers")}</span>
                    </Link>
                    <Link
                      to={ROUTES.admin.createUser}
                      className={cn(
                        "flex items-center gap-3 px-4 py-3 rounded-lg",
                        "transition-all duration-200",
                        "hover:bg-accent hover:text-accent-foreground",
                        "text-sm font-medium"
                      )}
                    >
                      <UserPlus className="h-4 w-4" />
                      <span>{t("admin.createUser")}</span>
                    </Link>
                  </nav>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="rounded-xl border bg-card shadow-sm p-8">
                  <div className="flex flex-col items-center justify-center text-center py-8">
                    <AlertCircle className="h-12 w-12 text-muted-foreground mb-4" />
                    <h3 className="text-lg font-semibold mb-2">{t("admin.accessDenied")}</h3>
                    <p className="text-sm text-muted-foreground max-w-md">
                      {t("admin.accessDeniedMessage")}
                    </p>
                  </div>
                </div>
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
}

