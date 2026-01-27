import { Moon, Sun, Monitor } from "lucide-react";
import { useTheme } from "@/hooks/useTheme";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface ThemeItemProps {
  themeValue: "light" | "dark" | "system";
  label: string;
  icon: React.ReactNode;
  activeTheme: string;
  onSetTheme: (theme: "light" | "dark" | "system") => void;
}

/**
 * A sub-component for rendering a single theme item in the dropdown.
 */
function ThemeItem({
  themeValue,
  label,
  icon,
  activeTheme,
  onSetTheme,
}: ThemeItemProps) {
  return (
    <DropdownMenuItem
      onClick={() => onSetTheme(themeValue)}
      className="cursor-pointer"
    >
      {icon}
      <span>{label}</span>
      {activeTheme === themeValue && <span className="ml-auto text-xs">✓</span>}
    </DropdownMenuItem>
  );
}

export function ThemeToggle() {
  const { theme, setTheme, resolvedTheme } = useTheme();

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="icon" className="relative">
          <Sun
            className={`h-[1.2rem] w-[1.2rem] transition-all ${resolvedTheme === "dark" ? "rotate-90 scale-0" : "rotate-0 scale-100"
              }`}
          />
          <Moon
            className={`absolute h-[1.2rem] w-[1.2rem] transition-all ${resolvedTheme === "dark" ? "rotate-0 scale-100" : "-rotate-90 scale-0"
              }`}
          />
          <span className="sr-only">Toggle theme</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <ThemeItem
          themeValue="light"
          label="Light"
          icon={<Sun className="mr-2 h-4 w-4" />}
          activeTheme={theme}
          onSetTheme={setTheme}
        />
        <ThemeItem
          themeValue="dark"
          label="Dark"
          icon={<Moon className="mr-2 h-4 w-4" />}
          activeTheme={theme}
          onSetTheme={setTheme}
        />
        <ThemeItem
          themeValue="system"
          label="System"
          icon={<Monitor className="mr-2 h-4 w-4" />}
          activeTheme={theme}
          onSetTheme={setTheme}
        />
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
