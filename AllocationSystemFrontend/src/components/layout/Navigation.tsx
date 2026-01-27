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
import { NAV_ITEMS, type NavItem } from "./nav-utils";

const NAV_ITEM_BASE_CLASS = "peer relative z-10 flex items-center gap-1 px-4 py-2 text-sm font-medium text-center whitespace-nowrap focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md transition-all duration-200"
const NAV_ITEM_ACTIVE_CLASS = "text-(--brand-strong) font-semibold cursor-default"
const NAV_ITEM_INACTIVE_CLASS = "text-muted-foreground cursor-pointer hover:text-foreground"

const MOBILE_NAV_ITEM_BASE = "w-full flex items-center justify-between px-4 py-3 rounded-md text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
const MOBILE_NAV_ITEM_ACTIVE = "text-foreground font-semibold bg-primary/10 border-l-4 border-primary"
const MOBILE_NAV_ITEM_INACTIVE = "text-muted-foreground cursor-pointer"

function DesktopNavItem({
  item,
  index,
  isActive,
  onPathActive,
  buttonRefs
}: {
  item: NavItem;
  index: number;
  isActive: boolean;
  onPathActive: (path?: string) => boolean;
  buttonRefs: React.MutableRefObject<(HTMLButtonElement | HTMLAnchorElement | null)[]>
}) {
  const { t } = useTranslation("common");
  const navigate = useNavigate();

  if (item.submenu) {
    return (
      <DropdownMenu key={item.translationKey}>
        <DropdownMenuTrigger asChild>
          <button
            ref={(el) => {
              if (buttonRefs.current) {
                buttonRefs.current[index] = el;
              }
            }}
            className={cn(NAV_ITEM_BASE_CLASS, isActive ? NAV_ITEM_ACTIVE_CLASS : NAV_ITEM_INACTIVE_CLASS)}
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
                onPathActive(subItem.path) && "bg-accent font-semibold"
              )}
            >
              {t(subItem.translationKey)}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    );
  }

  return (
    <NavLink
      key={item.path}
      ref={(el) => {
        if (buttonRefs.current) {
          buttonRefs.current[index] = el;
        }
      }}
      to={item.path!}
      end={item.path === ROUTES.main.dashboard}
      className={cn(NAV_ITEM_BASE_CLASS, "flex-1", isActive ? NAV_ITEM_ACTIVE_CLASS : NAV_ITEM_INACTIVE_CLASS)}
    >
      {t(item.translationKey)}
    </NavLink>
  );
}

function MobileNavItem({
  item,
  openDropdowns,
  onPathActive,
  onSubmenuActive,
  onToggle
}: {
  item: NavItem;
  openDropdowns: Set<string>;
  onPathActive: (path?: string) => boolean;
  onSubmenuActive: (item: NavItem) => boolean;
  onToggle: (key: string) => void;
}) {
  const { t } = useTranslation("common");
  const isActive = item.path ? onPathActive(item.path) : onSubmenuActive(item);

  if (item.submenu) {
    const isSubmenuOpen = openDropdowns.has(item.translationKey);
    return (
      <div key={item.translationKey}>
        <button
          onClick={() => { onToggle(item.translationKey); }}
          className={cn(MOBILE_NAV_ITEM_BASE, isActive ? MOBILE_NAV_ITEM_ACTIVE : MOBILE_NAV_ITEM_INACTIVE)}
        >
          <span>{t(item.translationKey)}</span>
          <ChevronDown className={cn("h-4 w-4 transition-transform duration-200", isSubmenuOpen && "rotate-180")} />
        </button>
        {isSubmenuOpen && (
          <div className="pl-4 mt-1 space-y-1">
            {item.submenu.map((subItem) => (
              <NavLink
                key={subItem.path}
                to={subItem.path!}
                className={cn("block px-4 py-2 rounded-md text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary", onPathActive(subItem.path) ? MOBILE_NAV_ITEM_ACTIVE : MOBILE_NAV_ITEM_INACTIVE)}
              >
                {t(subItem.translationKey)}
              </NavLink>
            ))}
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
      className={cn(MOBILE_NAV_ITEM_BASE, isActive ? MOBILE_NAV_ITEM_ACTIVE : MOBILE_NAV_ITEM_INACTIVE)}
    >
      {t(item.translationKey)}
    </NavLink>
  );
}

function useNavigationState() {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [openDropdowns, setOpenDropdowns] = useState<Set<string>>(new Set());

  const isPathActive = useCallback((path?: string) => {
    if (!path) {
      return false;
    }
    const currentPath = location.pathname;
    if (path === ROUTES.main.dashboard) {
      return currentPath === path;
    }
    return currentPath === path || currentPath.startsWith(path + "/");
  }, [location.pathname]);

  const isSubmenuActive = useCallback((item: NavItem) =>
    item.submenu ? item.submenu.some((subItem) => isPathActive(subItem.path)) : false
    , [isPathActive]);

  const activeIndex = useMemo(() => NAV_ITEMS.findIndex((item) =>
    item.submenu ? isSubmenuActive(item) : isPathActive(item.path)
  ), [isSubmenuActive, isPathActive]);

  useEffect(() => {
    setIsMobileMenuOpen(false);
    setOpenDropdowns(new Set());
  }, [location.pathname]);

  const handleMobileSubmenuToggle = (key: string) => {
    setOpenDropdowns((prev) => {
      const next = new Set(prev);
      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
      }
      return next;
    });
  };

  return {
    isMobileMenuOpen,
    setIsMobileMenuOpen,
    openDropdowns,
    activeIndex,
    isPathActive,
    isSubmenuActive,
    handleMobileSubmenuToggle
  };
}

export function Navigation() {
  const { t } = useTranslation("common");
  const navRef = useRef<HTMLDivElement>(null);
  const buttonRefs = useRef<(HTMLButtonElement | HTMLAnchorElement | null)[]>([]);
  const [indicatorStyle, setIndicatorStyle] = useState({ left: 0, width: 0 });

  const {
    isMobileMenuOpen,
    setIsMobileMenuOpen,
    openDropdowns,
    activeIndex,
    isPathActive,
    isSubmenuActive,
    handleMobileSubmenuToggle
  } = useNavigationState();

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
      setIndicatorStyle({ left: buttonRect.left - navRect.left, width: buttonRect.width });
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

  return (
    <>
      <nav ref={navRef} className="hidden lg:flex relative z-0 items-center justify-around overflow-x-auto scrollbar-hide min-w-0">
        {NAV_ITEMS.map((item, index) => (
          <DesktopNavItem key={item.translationKey} item={item} index={index} isActive={activeIndex === index} onPathActive={isPathActive} buttonRefs={buttonRefs} />
        ))}
        {activeIndex >= 0 && indicatorStyle.width > 0 && (
          <span
            className="absolute -z-10 h-full origin-center scale-x-110 scale-y-125 rounded-full border border-(--brand-border) bg-(--brand-soft) shadow-[0_10px_30px_-18px_var(--brand)] transition-[left,width] duration-300 ease-out pointer-events-none"
            style={{ left: `${indicatorStyle.left}px`, width: `${indicatorStyle.width}px` }}
            aria-hidden="true"
          />
        )}
      </nav>

      <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)} aria-label="Toggle menu">
        {isMobileMenuOpen ? <X className="h-6 w-6 rotate-90" /> : <Menu className="h-6 w-6" />}
      </Button>

      <div className={cn("lg:hidden fixed left-0 right-0 top-16 z-50 border-b bg-background backdrop-blur shadow-lg transition-all duration-300 ease-in-out", isMobileMenuOpen ? "opacity-100 translate-y-0 visible" : "opacity-0 -translate-y-4 invisible pointer-events-none")}>
        <Container>
          <nav className="py-4 space-y-1 max-h-[calc(100vh-4rem)] overflow-y-auto">
            {NAV_ITEMS.map((item) => (
              <MobileNavItem
                key={item.translationKey}
                item={item}
                openDropdowns={openDropdowns}
                onPathActive={isPathActive}
                onSubmenuActive={isSubmenuActive}
                onToggle={handleMobileSubmenuToggle}
              />
            ))}
          </nav>
        </Container>
      </div>
      {isMobileMenuOpen && <div className="lg:hidden fixed inset-0 bg-background/80 backdrop-blur-sm z-40 top-16" onClick={() => setIsMobileMenuOpen(false)} aria-hidden="true" />}
    </>
  );
}


