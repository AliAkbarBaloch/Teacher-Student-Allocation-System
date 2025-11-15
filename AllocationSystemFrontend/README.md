# Internship Allocation System – Frontend

## 📌 Overview

This frontend is built with **React**, **TypeScript**, and **Vite**, following a modern, scalable, and maintainable architecture.  
It uses **feature-based organization**, **clean separation of concerns**, and includes a fully configured **testing setup** using **Vitest** and **React Testing Library**.

---

# 🏗️ Frontend Architecture

The project follows a domain-driven, feature-based structure:

```
src/

├── app/                          # Root application setup
│   ├── App.tsx
│   ├── main.tsx
│   └── routes.tsx
│
├── components/                   # Reusable and shared components
│   ├── ui/                       # UI elements
│   ├── layout/                   # Header, Footer, Layout components
│   └── common/                   # Shared utilities like Loader, ThemeToggle
│
├── features/                     # Domain-driven feature modules
│   ├── auth/
│   └── products/
│
├── hooks/                        # Global reusable hooks
│
├── lib/                          # Setup libraries (i18n, axios, utils)
│
├── providers/                    # React context providers
│
├── pages/                        # Route-level pages
│
├── store/                        # Zustand / global state
│
├── types/                        # Shared TypeScript types
│
├── assets/                       # Images, fonts, icons
│
└── config/                       # Environment, constants, route config
```

### 🧭 Architecture Principles

- **Feature-based organization**
- **Strong separation of concerns**
- **Reusable UI components**
- **Type safety with TypeScript**
- **Scalability-first folder structure**

---

# 📦 Dependencies

### Core
- **React 19**
- **TypeScript**
- **Vite**

### Styling
- **Tailwind CSS**
- **clsx**, **tailwind-merge**
- **lucide-react** icons

### Development Tools
- **ESLint** + TypeScript ESLint
- **Vitest + React Testing Library**
- **jsdom** for browser-like environment

---

# 🚀 Getting Started

### 1️⃣ Navigate to frontend directory

```bash
cd AllocationSystemFrontend
```

### 2️⃣ Install dependencies

```bash
npm install
```

### 3️⃣ Run development server

```bash
npm run dev
```

App runs at:

```
http://localhost:5173
```

---

# 🏗️ Build for Production

```bash
npm run build
```

Outputs to:

```
dist/
```

Preview production build:

```bash
npm run preview
```

---

# 🧹 Linting

Run lint:

```bash
npm run lint
```

Auto-fix:

```bash
npm run lint -- --fix
```

---

# 🧪 Frontend Testing

The project uses:

| Tool | Purpose |
|------|---------|
| **Vitest** | Test runner & assertion library |
| **React Testing Library** | Rendering + interaction |
| **@testing-library/jest-dom** | Extra DOM matchers |
| **jsdom** | Browser-like environment |

---

# ⚙️ Test Configuration

### `vitest.config.ts`

Includes:
- `environment: "jsdom"` — enables DOM APIs
- `globals: true` — allows `describe`, `it`, `expect` globally
- `setupFiles: "./src/setupTests.ts"` — global setup
- Alias support for `"@"` → `src/`

### `src/setupTests.ts`

```ts
import "@testing-library/jest-dom/vitest";
```

Provides matchers like:
- `toBeInTheDocument()`
- `toHaveTextContent()`
- `toBeVisible()`

---

# 📁 Test File Structure

Tests live **next to the components they test**:

Patterns:
```
src/**/*.test.ts
src/**/*.test.tsx
```

Examples:
```
src/app/App.test.tsx
src/components/ui/Button/Button.test.tsx
src/pages/home/HomePage.test.tsx
```

---

# 🧪 Example Test (App Component)

```tsx
import { render, screen } from "@testing-library/react";
import App from "./App";

vi.mock("@/components/layout/MainLayout", () => ({
  default: () => <div>Main Layout Rendered</div>,
}));

describe("App", () => {
  it("renders the main layout", () => {
    render(<App />);
    expect(screen.getByText(/main layout rendered/i)).toBeInTheDocument();
  });
});
```

This verifies that `<App />` correctly renders layout.

---

# ▶️ Running Tests

### Watch mode

```bash
npm test
```

### Single run (CI/CD)

```bash
npm run vitest:run
```

### Linting

```bash
npm run lint
```

---

# ✅ Summary

This README includes:
- Full architecture overview  
- Setup instructions  
- Testing configuration  
- Example test  
- Development workflow  


