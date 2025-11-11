import { it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import React from "react";

it("renders text from test folder", () => {
  // Keep React import for future tests/components that depend on the React runtime.
  void React;
  render(<div>frontend test folder</div>);
  expect(screen.getByText("frontend test folder")).toBeInTheDocument();
});
