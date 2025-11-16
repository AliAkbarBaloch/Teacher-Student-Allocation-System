# Frontend Security Guidelines (Vite)

## 1. Environment Variables & Secrets

- Real secrets must **never** be committed to the frontend or exposed to the browser.
- Vite exposes only variables starting with the `VITE_` prefix.
- Use `.env.example` as a template and create your own `.env.local` for local development.
- Only public configuration should appear in Vite env vars.
- Real secrets must be stored on the backend or in CI/CD, not in the frontend.

### Required Variables
Example from `.env.example`:
```
VITE_API_BASE_URL=https://api.example.com
VITE_PUBLIC_MAP_TOKEN=replace-me
```

### Do NOT commit:
```
.env
.env.local
.env.*.local
```

---

## 2. Dependency Security (Snyk)

- Snyk is installed as a dev dependency and runs in CI.
- The CI job performs:
  - `npm ci`
  - Snyk authentication using `$SNYK_TOKEN`
  - Dependency scanning with `snyk test`
- CI fails if vulnerabilities of **medium or higher** severity are found.

---

## 3. Authentication & Token Security

- Avoid storing sensitive auth tokens in `localStorage` or `sessionStorage`.
- Prefer HttpOnly cookies.
- Never include tokens or secrets in URLs.
- Clear authentication state on logout.

---

## 4. Safe Coding Practices

- Avoid `dangerouslySetInnerHTML` unless sanitized.
- Validate user input on the backend; frontend validation is for UX only.
- Be careful when building URLs or query strings with user data.
- Keep dependencies up to date and remove unused libraries.

---

## 5. Checklist for New Frontend Work

- [ ] No secrets committed.
- [ ] `.env` files ignored by Git.
- [ ] `.env.example` updated when new env vars are added.
- [ ] Snyk test passes in CI.
- [ ] Authentication & token handling follow guidelines.