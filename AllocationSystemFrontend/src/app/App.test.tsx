//import { describe, it, expect } from 'vitest';
import {render, screen} from "@testing-library/react";
import App from "./App";

// Mock the MainLayout since we don't need full layout logic for this basic test
vi.mock("@/components/layout/MainLayout", () => ({
  default: () => <div>Main Layout Rendered</div>,
}));

describe("App", () => {
  it("renders the main layout", () => {
    render(<App />);
    expect(screen.getByText(/main layout rendered/i)).toBeInTheDocument();
  });
});
