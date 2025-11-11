export const ROUTES = {
  main: {
    home: "/",
    schools: "/schools",
    teachers_pools: "/teachers-pools",
    teacher_management: "/teacher-management",
  },
  configuration: {
    budget: "/budgets",
    subjects: "/subjects",
    internships: "/internships",
  },
  operations: {
    allocation: "/allocation",
    reports: "/reports",
    archives: "/archives",
  },
} as const;
