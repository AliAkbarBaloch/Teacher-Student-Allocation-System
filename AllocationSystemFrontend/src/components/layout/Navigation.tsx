import { NavLink, useLocation } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { cn } from "@/lib/utils";
import { useTranslation } from "react-i18next";
import { useState, useRef, useEffect, useMemo } from "react";
import { Menu, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Container } from "./Container";

export function Navigation() {
  const { t } = useTranslation("common");
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navRef = useRef<HTMLDivElement>(null);
  const buttonRefs = useRef<(HTMLAnchorElement | null)[]>([]);
  const [indicatorStyle, setIndicatorStyle] = useState<{
    left: number;
    width: number;
  }>({ left: 0, width: 0 });

  const navItems = useMemo(
    () => [
      { path: ROUTES.main.home, translationKey: "navigation.home" },
      { path: ROUTES.main.schools, translationKey: "navigation.schools" },
      { path: ROUTES.main.teachers_pools, translationKey: "navigation.teachersPools" },
      { path: ROUTES.main.teacher_management, translationKey: "navigation.teacherManagement" },
      { path: ROUTES.configuration.budget, translationKey: "navigation.budget" },
      { path: ROUTES.configuration.subjects, translationKey: "navigation.subjects" },
      { path: ROUTES.configuration.internships, translationKey: "navigation.internships" },
      { path: ROUTES.operations.allocation, translationKey: "navigation.allocation" },
      { path: ROUTES.operations.reports, translationKey: "navigation.reports" },
      { path: ROUTES.operations.archives, translationKey: "navigation.archives" },
    ],
    []
  );

  // Find active index
  const activeIndex = useMemo(() => {
    const currentPath = location.pathname;
    return navItems.findIndex((item) => {
      if (item.path === ROUTES.main.home) {
        return currentPath === item.path;
      }
      return currentPath === item.path || currentPath.startsWith(item.path + "/");
    });
  }, [location.pathname, navItems]);

  // Update indicator position (minimal JS, CSS handles animation)
  useEffect(() => {
    if (activeIndex === -1 || !navRef.current || !buttonRefs.current[activeIndex]) {
      setIndicatorStyle({ left: 0, width: 0 });
      return;
    }

    const updateIndicator = () => {
      const activeButton = buttonRefs.current[activeIndex];
      if (!activeButton || !navRef.current) return;

      const navRect = navRef.current.getBoundingClientRect();
      const buttonRect = activeButton.getBoundingClientRect();
      setIndicatorStyle({
        left: buttonRect.left - navRect.left,
        width: buttonRect.width,
      });
    };

    const timeoutId = setTimeout(updateIndicator, 0);
    window.addEventListener("resize", updateIndicator);
    const navElement = navRef.current;
    if (navElement) navElement.addEventListener("scroll", updateIndicator);

    return () => {
      clearTimeout(timeoutId);
      window.removeEventListener("resize", updateIndicator);
      if (navElement) navElement.removeEventListener("scroll", updateIndicator);
    };
  }, [activeIndex, t]);

  // Close mobile menu on route change
  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  return (
    <>
      {/* Desktop Navigation */}
      <nav
        ref={navRef}
        className="hidden lg:flex relative z-0 items-center justify-around overflow-x-auto scrollbar-hide min-w-0"
      >
        {navItems.map((item, index) => {
          const isActive = activeIndex === index;
          return (
            <NavLink
              key={item.path}
              ref={(el) => {
                buttonRefs.current[index] = el;
              }}
              to={item.path}
              end={item.path === ROUTES.main.home}
              className={cn(
                "peer relative z-10 flex-1 px-4 py-2 text-sm font-medium text-center whitespace-nowrap",
                "transition-all duration-200 ease-out",
                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md",
                isActive
                  ? "text-foreground font-semibold cursor-default"
                  : "text-muted-foreground hover:text-foreground cursor-pointer hover:scale-105 active:scale-95"
              )}
            >
              {t(item.translationKey)}
            </NavLink>
          );
        })}

        {/* Sliding Indicator - CSS handles animation */}
        {activeIndex >= 0 && indicatorStyle.width > 0 && (
          <span
            className="absolute -z-10 h-full origin-center scale-x-110 scale-y-125 rounded-full bg-muted transition-[left,width] duration-300 ease-out pointer-events-none"
            style={{
              left: `${indicatorStyle.left}px`,
              width: `${indicatorStyle.width}px`,
            }}
            aria-hidden="true"
          />
        )}
      </nav>

      {/* Mobile Menu Button */}
      <Button
        variant="ghost"
        size="icon"
        className="lg:hidden"
        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        aria-label="Toggle menu"
      >
        {isMobileMenuOpen ? (
          <X className="h-6 w-6 transition-transform duration-300 rotate-90" />
        ) : (
          <Menu className="h-6 w-6 transition-transform duration-300" />
        )}
      </Button>

      {/* Mobile Navigation */}
      <div
        className={cn(
          "lg:hidden fixed left-0 right-0 top-16 z-50 border-b bg-background backdrop-blur shadow-lg",
          "transition-all duration-300 ease-in-out",
          isMobileMenuOpen
            ? "opacity-100 translate-y-0 visible"
            : "opacity-0 -translate-y-4 invisible pointer-events-none"
        )}
      >
        <Container>
          <nav className="py-4 space-y-1 max-h-[calc(100vh-4rem)] overflow-y-auto">
          {navItems.map((item, index) => {
            const isActive = activeIndex === index;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === ROUTES.main.home}
                className={cn(
                  "block px-4 py-3 rounded-md text-sm font-medium",
                  "transition-all duration-200 ease-out",
                  "transform hover:translate-x-1 hover:scale-[1.02] active:scale-95",
                  "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                  isActive
                    ? "text-foreground font-semibold bg-primary/10 border-l-4 border-primary"
                    : "text-muted-foreground hover:text-foreground hover:bg-accent cursor-pointer"
                )}
              >
                {t(item.translationKey)}
              </NavLink>
            );
          })}
          </nav>
        </Container>
      </div>

      {/* Mobile Menu Overlay */}
      {isMobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-background/80 backdrop-blur-sm z-40 top-16"
          onClick={() => setIsMobileMenuOpen(false)}
          aria-hidden="true"
        />
      )}
    </>
  );
}

