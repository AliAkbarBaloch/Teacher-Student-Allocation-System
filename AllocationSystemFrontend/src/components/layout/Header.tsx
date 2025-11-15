import { ThemeToggle } from "../common/ThemeToggle";
import { LanguageSwitcher } from "../common/LanguageSwitcher";
import { Logo } from "../common/Logo";
import { Navigation } from "./Navigation";
import { UserMenu } from "./UserMenu";
import { Container } from "./Container";
import { useAuth } from "@/features/auth/hooks/useAuth";

export default function Header() {
  const { isAuthenticated } = useAuth();

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <Container className="flex h-16 items-center justify-between">
        {/* Left Side - Logo and Navigation */}
        <div className="flex items-center gap-4 lg:gap-8">
          <Logo size="sm" showText={false} className="flex-shrink-0" />
          <Navigation />
        </div>

        {/* Right Side Actions */}
        <div className="flex items-center gap-2 lg:gap-3">
          <LanguageSwitcher />
          <ThemeToggle />
          {isAuthenticated && <UserMenu />}
        </div>
      </Container>
    </header>
  );
}
