import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { ROUTES } from "@/config/routes";
import { cn } from "@/lib/utils";
import { useTranslation } from "react-i18next";
import { useState, useRef, useEffect, useMemo, useCallback } from "react";
import { Menu, X, ChevronDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Container } from "./Container";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

interface NavItem {
  path?: string;
  translationKey: string;
  submenu?: NavItem[];
}

export function Navigation() {
  const { t } = useTranslation("common");
  const location = useLocation();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [openDropdowns, setOpenDropdowns] = useState<Set<string>>(new Set());
  const navRef = useRef<HTMLDivElement>(null);
  const buttonRefs = useRef<(HTMLButtonElement | HTMLAnchorElement | null)[]>([]);
  const [indicatorStyle, setIndicatorStyle] = useState<{
    left: number;
    width: number;
  }>({ left: 0, width: 0 });

  const navItems = useMemo<NavItem[]>(
    () => [
      { path: ROUTES.main.dashboard, translationKey: "navigation.dashboard" },
      { path: ROUTES.main.allocationReport, translationKey: "navigation.allocationReport" },
      {
        translationKey: "navigation.baseData",
        submenu: [
          { path: ROUTES.baseData.academicYears, translationKey: "navigation.baseDataAcademicYears" },
          { path: ROUTES.baseData.subjectCategories, translationKey: "navigation.baseDataSubjectCategories" },
          { path: ROUTES.baseData.subjects, translationKey: "navigation.baseDataSubjects" },
          { path: ROUTES.baseData.schools, translationKey: "navigation.baseDataSchools" },
          { path: ROUTES.baseData.teachers, translationKey: "navigation.baseDataTeachers" },
          { path: ROUTES.baseData.internshipTypes, translationKey: "navigation.baseDataInternshipTypes" },
        ],
      },
      {
        translationKey: "navigation.teacherManagement",
        submenu: [
          { path: ROUTES.teacherManagement.teacherSubjects, translationKey: "navigation.teacherManagementTeacherSubjects" },
          { path: ROUTES.teacherManagement.teacherAvailability, translationKey: "navigation.teacherManagementTeacherAvailability" },
          { path: ROUTES.teacherManagement.teacherSubmissions, translationKey: "navigation.teacherManagementTeacherSubmissions" },
        ],
      },
      {
        translationKey: "navigation.internshipDemand",
        submenu: [
          { path: ROUTES.internshipDemand.demandPerYear, translationKey: "navigation.internshipDemandDemandPerYear" },
        ],
      },
      {
        translationKey: "navigation.allocationPlanning",
        submenu: [
          { path: ROUTES.allocationPlanning.allocationPlans, translationKey: "navigation.allocationPlanningAllocationPlans" },
          { path: ROUTES.allocationPlanning.teacherAssignments, translationKey: "navigation.allocationPlanningTeacherAssignments" },
          { path: ROUTES.allocationPlanning.creditHourTracking, translationKey: "navigation.allocationPlanningCreditHourTracking" },
          { path: ROUTES.baseData.zoneConstraints, translationKey: "navigation.allocationPlanningZoneConstraints" },
          { path: ROUTES.allocationPlanning.planChangeLogs, translationKey: "navigation.allocationPlanningPlanChangeLogs" },
        ],
      },
      {
        translationKey: "navigation.reports",
        submenu: [
          { path: ROUTES.reports.allocationReports, translationKey: "navigation.reportsAllocationReports" },
          { path: ROUTES.reports.teacherReports, translationKey: "navigation.reportsTeacherReports" },
          { path: ROUTES.reports.schoolReports, translationKey: "navigation.reportsSchoolReports" },
          { path: ROUTES.reports.auditReports, translationKey: "navigation.reportsAuditReports" },
        ],
      },
    ],
    []
  );

  // Check if a path is active
  const isPathActive = useCallback((path?: string): boolean => {
    if (!path) return false;
    const currentPath = location.pathname;
    if (path === ROUTES.main.dashboard) {
      return currentPath === path;
    }
    return currentPath === path || currentPath.startsWith(path + "/");
  }, [location.pathname]);

  // Check if any submenu item is active
  const isSubmenuActive = useCallback((item: NavItem): boolean => {
    if (!item.submenu) return false;
    return item.submenu.some((subItem) => isPathActive(subItem.path));
  }, [isPathActive]);

  // Find active index for simple links
  const activeIndex = useMemo(() => {
    return navItems.findIndex((item) => {
      if (item.submenu) {
        return isSubmenuActive(item);
      }
      return isPathActive(item.path);
    });
  }, [navItems, isPathActive, isSubmenuActive]);

  // Update indicator position
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
    setOpenDropdowns(new Set());
  }, [location.pathname]);

  const handleMobileSubmenuToggle = (key: string) => {
    const newOpen = new Set(openDropdowns);
    if (newOpen.has(key)) {
      newOpen.delete(key);
    } else {
      newOpen.add(key);
    }
    setOpenDropdowns(newOpen);
  };

  return (
    <>
      {/* Desktop Navigation */}
      <nav
        ref={navRef}
        className="hidden lg:flex relative z-0 items-center justify-around overflow-x-auto scrollbar-hide min-w-0"
      >
        {navItems.map((item, index) => {
          const isActive = activeIndex === index;
          
          if (item.submenu) {
            // Dropdown menu item
            return (
              <DropdownMenu key={item.translationKey}>
                <DropdownMenuTrigger asChild>
                  <button
                    ref={(el) => {
                      buttonRefs.current[index] = el;
                    }}
                  className={cn(
                    "peer relative z-10 flex items-center gap-1 px-4 py-2 text-sm font-medium text-center whitespace-nowrap",
                    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md",
                    isActive
                      ? "text-(--brand-strong) font-semibold cursor-default"
                      : "text-muted-foreground cursor-pointer"
                  )}
                  >
                    {t(item.translationKey)}
                    <ChevronDown className="h-4 w-4" />
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="start" className="min-w-[200px]">
                  {item.submenu.map((subItem) => (
                    <DropdownMenuItem
                      key={subItem.path}
                      onClick={() => subItem.path && navigate(subItem.path)}
                      className={cn(
                        "cursor-pointer transition-none hover:bg-transparent",
                        isPathActive(subItem.path) && "bg-accent font-semibold"
                      )}
                    >
                      {t(subItem.translationKey)}
                    </DropdownMenuItem>
                  ))}
                </DropdownMenuContent>
              </DropdownMenu>
            );
          }

          // Simple link item
          return (
            <NavLink
              key={item.path}
              ref={(el) => {
                buttonRefs.current[index] = el;
              }}
              to={item.path!}
              end={item.path === ROUTES.main.dashboard}
              className={cn(
                "peer relative z-10 flex-1 px-4 py-2 text-sm font-medium text-center whitespace-nowrap",
                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md",
                isActive
                  ? "text-(--brand-strong) font-semibold cursor-default"
                  : "text-muted-foreground cursor-pointer"
              )}
            >
              {t(item.translationKey)}
            </NavLink>
          );
        })}

        {/* Sliding Indicator - CSS handles animation */}
        {activeIndex >= 0 && indicatorStyle.width > 0 && (
          <span
            className="absolute -z-10 h-full origin-center scale-x-110 scale-y-125 rounded-full border border-(--brand-border) bg-(--brand-soft) shadow-[0_10px_30px_-18px_var(--brand)] transition-[left,width] duration-300 ease-out pointer-events-none"
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
            {navItems.map((item) => {
              const isActive = item.path ? isPathActive(item.path) : isSubmenuActive(item);
              
              if (item.submenu) {
                const isSubmenuOpen = openDropdowns.has(item.translationKey);
                return (
                  <div key={item.translationKey}>
                    <button
                      onClick={() => handleMobileSubmenuToggle(item.translationKey)}
                      className={cn(
                        "w-full flex items-center justify-between px-4 py-3 rounded-md text-sm font-medium",
                        "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                        isActive
                          ? "text-foreground font-semibold bg-primary/10 border-l-4 border-primary"
                          : "text-muted-foreground cursor-pointer"
                      )}
                    >
                      <span>{t(item.translationKey)}</span>
                      <ChevronDown
                        className={cn(
                          "h-4 w-4 transition-transform duration-200",
                          isSubmenuOpen && "rotate-180"
                        )}
                      />
                    </button>
                    {isSubmenuOpen && (
                      <div className="pl-4 mt-1 space-y-1">
                        {item.submenu.map((subItem) => {
                          const isSubActive = isPathActive(subItem.path);
                          return (
                            <NavLink
                              key={subItem.path}
                              to={subItem.path!}
                              className={cn(
                                "block px-4 py-2 rounded-md text-sm",
                                "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                                isSubActive
                                  ? "text-foreground font-semibold bg-primary/10 border-l-4 border-primary"
                                  : "text-muted-foreground cursor-pointer"
                              )}
                            >
                              {t(subItem.translationKey)}
                            </NavLink>
                          );
                        })}
                      </div>
                    )}
                  </div>
                );
              }

              return (
                <NavLink
                  key={item.path}
                  to={item.path!}
                  end={item.path === ROUTES.main.dashboard}
                  className={cn(
                    "block px-4 py-3 rounded-md text-sm font-medium",
                    "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary",
                    isActive
                      ? "text-foreground font-semibold bg-primary/10 border-l-4 border-primary"
                      : "text-muted-foreground cursor-pointer"
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
