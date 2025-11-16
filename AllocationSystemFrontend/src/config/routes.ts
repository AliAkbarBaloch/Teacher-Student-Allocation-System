export const ROUTES = {
  auth: {
    login: "/login",
    forgotPassword: "/forgot-password",
    resetPassword: "/reset-password",
  },
  main: {
    home: "/home",
    schools: "/schools",
    teachers_pools: "/teachers-pools",
    teacher_management: "/teacher-management",
    settings: "/settings",
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
  admin: {
    users: "/admin/users",
    createUser: "/admin/users/create",
    editUser: "/admin/users/edit/:id",
  },
} as const;
