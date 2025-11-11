import { NavLink } from "react-router-dom";
import { ROUTES } from "../../config/routes";
import clsx from "clsx";
import { ThemeToggle } from "../common/ThemeToggle";

export default function Header() {
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    clsx(
      "px-3 py-2 rounded-md dark:hover:text-gray-800",
      isActive ? "bg-gray-400" : "hover:bg-gray-100"
    );

  return (
    <header className="border-b">
      <nav className="container mx-auto flex gap-2 p-3">
            <NavLink to={ROUTES.main.home} className={linkClass}>
              Home
            </NavLink>
            <NavLink to={ROUTES.main.schools} className={linkClass}>
              Schools
            </NavLink>
            <NavLink to={ROUTES.main.teachers_pools} className={linkClass}>
              Teachers Pools
            </NavLink>
            <NavLink to={ROUTES.main.teacher_management} className={linkClass}>
              Teacher Management
            </NavLink>
            <NavLink to={ROUTES.configuration.budget} className={linkClass}>
              Budget
            </NavLink>
            <NavLink to={ROUTES.configuration.subjects} className={linkClass}>
              Subjects
            </NavLink>
            <NavLink to={ROUTES.configuration.internships} className={linkClass}>
              Internships
            </NavLink>
            <NavLink to={ROUTES.operations.allocation} className={linkClass}>
              Allocation
            </NavLink>
            <NavLink to={ROUTES.operations.reports} className={linkClass}>
              Reports
            </NavLink>
            <NavLink to={ROUTES.operations.archives} className={linkClass}>
              Archives
            </NavLink>
            <ThemeToggle />
      </nav>
    </header>
  );
}
