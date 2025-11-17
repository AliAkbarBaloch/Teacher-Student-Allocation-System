import { useTranslation } from "react-i18next";
import { cn } from "@/lib/utils";
import { User, Lock, type LucideIcon } from "lucide-react";
import type { SettingsSection } from "@/features/settings/types/settings.types";


interface SettingsSidebarProps {
  activeSection: SettingsSection;
  onSectionChange: (section: SettingsSection) => void;
}

export function SettingsSidebar({ activeSection, onSectionChange }: SettingsSidebarProps) {
  const { t } = useTranslation("settings");


  const sections: { id: SettingsSection; icon: LucideIcon; translationKey: string }[] = [
    { id: "profile", icon: User, translationKey: "sidebar.profile" },
    { id: "password", icon: Lock, translationKey: "sidebar.password" },
  ];

  return (
    <aside className="w-full sm:w-56 lg:w-64 shrink-0">
      <nav className="space-y-1" role="navigation" aria-label="Settings navigation">
        {sections.map((section) => {
          const Icon = section.icon;
          const isActive = activeSection === section.id;
          
          return (
            <button
              key={section.id}
              onClick={() => onSectionChange(section.id)}
              className={cn(
                "w-full flex items-center gap-3 px-3 sm:px-4 py-3 rounded-lg text-sm font-medium",
                "transition-all duration-200",
                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2",
                isActive
                  ? "bg-primary text-primary-foreground shadow-sm"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )}
              aria-label={t(section.translationKey)}
              aria-current={isActive ? "page" : undefined}
            >
              <Icon className="h-5 w-5 shrink-0" aria-hidden="true" />
              <span className="truncate">{t(section.translationKey)}</span>
            </button>
          );
        })}
      </nav>
    </aside>
  );
}

