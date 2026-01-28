import { Link } from "react-router-dom";
import {
    Breadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
import { ThemeToggle } from "../common/ThemeToggle";
import { LanguageSwitcher } from "../common/LanguageSwitcher";
import type { BreadcrumbItem as BreadcrumbType } from "@/hooks/useBreadcrumbs";

interface SidebarHeaderProps {
    breadcrumbs: BreadcrumbType[];
}

export function SidebarHeader({ breadcrumbs }: SidebarHeaderProps) {
    return (
        <header className="flex h-16 shrink-0 items-center gap-2 border-b border-(--brand-border) backdrop-blur dark:bg-neutral-900">
            <div className="flex items-center gap-2 px-4 flex-1">
                <SidebarTrigger className="-ml-1" />
                <Separator
                    orientation="vertical"
                    className="mr-2 data-[orientation=vertical]:h-4"
                />
                <Breadcrumb>
                    <BreadcrumbList>
                        {breadcrumbs.map((crumb, index) => (
                            <div key={index} className="flex items-center">
                                {index > 0 && <BreadcrumbSeparator className="hidden md:block" />}
                                <BreadcrumbItem className={index === 0 ? "hidden md:block" : ""}>
                                    {crumb.path && index < breadcrumbs.length - 1 ? (
                                        <BreadcrumbLink asChild>
                                            <Link
                                                to={crumb.path}
                                                className="text-muted-foreground transition-colors hover:text-(--brand-strong)"
                                            >
                                                {crumb.label}
                                            </Link>
                                        </BreadcrumbLink>
                                    ) : (
                                        <BreadcrumbPage className="text-foreground font-medium">
                                            {crumb.label}
                                        </BreadcrumbPage>
                                    )}
                                </BreadcrumbItem>
                            </div>
                        ))}
                    </BreadcrumbList>
                </Breadcrumb>
            </div>
            <div className="flex items-center gap-2 px-4">
                <LanguageSwitcher />
                <ThemeToggle />
            </div>
        </header>
    );
}
