import { render, screen, fireEvent } from "@testing-library/react";
import { Avatar } from "../Avatar";
import { describe, it, expect, vi } from "vitest";

// Mock useTranslation
vi.mock("react-i18next", () => ({
    useTranslation: () => ({
        t: (key: string) => key,
    }),
}));

describe("Avatar", () => {
    it("renders user initials correctly with name", () => {
        render(<Avatar name="John Doe" />);
        expect(screen.getByText("JD")).toBeDefined();
    });

    it("renders user initials correctly with email", () => {
        render(<Avatar email="jane@example.com" />);
        expect(screen.getByText("J")).toBeDefined();
    });

    it("renders default initial when no name or email provided", () => {
        render(<Avatar />);
        expect(screen.getByText("U")).toBeDefined();
    });

    it("shows edit button when showEditButton is true", () => {
        render(<Avatar showEditButton={true} />);
        expect(screen.getByLabelText("Change avatar")).toBeDefined();
    });

    it("calls onEditClick when edit button is clicked", () => {
        const onEditClick = vi.fn();
        render(<Avatar showEditButton={true} onEditClick={onEditClick} />);
        fireEvent.click(screen.getByLabelText("Change avatar"));
        expect(onEditClick).toHaveBeenCalled();
    });
});
