# Internship Allocation System

## Frontend Architecture

The frontend follows a feature-based, domain-driven architecture with clear separation of concerns:

```
src/

├── app/                          # Root application setup
│   ├── App.tsx
│   ├── main.tsx
│   └── routes.tsx                # Central route definitions
│
├── components/                   # Reusable and shared components
│   ├── ui/                       # UI components (buttons, inputs, modals, etc.)
│   │   ├── Button/
│   │   │   ├── Button.tsx
│   │   │   ├── Button.test.tsx
│   │   │   └── index.ts
│   │   └── Input/
│   ├── layout/                   # Layout components
│   │   ├── Header.tsx
│   │   ├── Footer.tsx
│   │   └── Sidebar.tsx
│   └── common/                   # Other shared components (not part of UI library)
│       ├── ThemeToggle.tsx
│       ├── LanguageSwitcher.tsx
│       └── Loader.tsx
│
├── features/                     # Domain-driven feature modules
│   ├── auth/
│   │   ├── components/
│   │   │   ├── LoginForm.tsx
│   │   │   └── RegisterForm.tsx
│   │   ├── hooks/
│   │   │   └── useAuth.ts
│   │   ├── services/
│   │   │   └── authService.ts
│   │   ├── types/
│   │   │   └── auth.types.ts
│   │   └── index.ts
│   └── products/
│       ├── components/
│       │   └── ProductCard.tsx
│       ├── hooks/
│       │   └── useProducts.ts
│       ├── services/
│       │   └── productService.ts
│       └── types/
│           └── product.types.ts
│
├── hooks/                        # Global custom hooks (non-feature specific)
│   ├── useTheme.ts
│   ├── useMediaQuery.ts
│   └── useLocalStorage.ts
│
├── lib/                          # Library setup, utilities, and global helpers
│   ├── i18n/                     # Multi-language setup
│   │   ├── index.ts
│   │   ├── locales/
│   │   │   ├── de/translation.json
│   │   │   └── en/translation.json
│   ├── theme/                    # Theme handling (light/dark)
│   │   ├── index.ts
│   │   └── useThemeMode.ts
│   ├── axios.ts                  # Configured Axios instance
│   └── utils.ts                  # Global utility functions
│
├── providers/                    # React context providers
│   ├── ThemeProvider.tsx
│   ├── AuthProvider.tsx
│   └── I18nProvider.tsx
│
├── pages/                        # Page-level route components
│   ├── home/
│   │   ├── HomePage.tsx
│   │   └── HomePage.test.tsx
│   ├── profile/
│   │   └── ProfilePage.tsx
│   └── settings/
│       └── SettingsPage.tsx
│
├── store/                        # State management (Zustand, Redux, or Context)
│   ├── slices/
│   │   ├── userSlice.ts
│   │   └── appSlice.ts
│   └── index.ts
│
├── types/                        # Shared TypeScript type definitions
│   ├── user.types.ts
│   ├── api.types.ts
│   └── common.types.ts
│
│
├── assets/                       # Static assets
│   ├── images/
│   ├── icons/
│   └── fonts/
│
├── tests/                        # Integration and e2e test setup
│   ├── setup.ts
│   └── mocks/
│
└── config/                       # App configuration files (env, constants, etc.)
    ├── env.ts
    ├── constants.ts
    └── routes.ts
```

### Architecture Principles

- **Feature-based organization**: Each feature module is self-contained with its own components, hooks, services, and types
- **Separation of concerns**: Clear boundaries between UI components, business logic, and data fetching
- **Reusability**: Shared components and utilities are organized in dedicated directories
- **Type safety**: TypeScript types are defined at feature and global levels
- **Scalability**: Structure supports growth and easy addition of new features

## Dependencies

### Core
- **React** 19.1.1 - UI library
- **TypeScript** 5.9.3 - Type safety
- **Vite** 7.1.7 - Build tool and dev server

### Styling
- **Tailwind CSS** 4.1.17 - Utility-first CSS framework
- **tailwind-merge** - Tailwind class merging utility
- **clsx** - Conditional class names

### UI Components
- **lucide-react** - Icon library

### Development Tools
- **ESLint** - Code linting
- **TypeScript ESLint** - TypeScript-specific linting rules

## Getting Started

### Prerequisites

- Node.js (v18 or higher recommended)
- npm or yarn package manager

### Installation

1. Navigate to the frontend directory:
```bash
cd AllocationSystemFrontend
```

2. Install dependencies:
```bash
npm install
```

## Development

### Run Development Server

Start the development server with hot module replacement:

```bash
npm run dev
```

The application will be available at `http://localhost:5173` (or the next available port).

### Build for Production

Build the application for production:

```bash
npm run build
```

This command:
1. Type-checks the code using TypeScript (`tsc -b`)
2. Builds optimized production bundles using Vite

The output will be in the `dist/` directory.

### Preview Production Build

Preview the production build locally:

```bash
npm run preview
```

This serves the production build from the `dist/` directory.

### Lint Code

Run ESLint to check for code quality issues:

```bash
npm run lint
```

To automatically fix linting issues (if supported by your ESLint configuration):

```bash
npm run lint -- --fix
```
