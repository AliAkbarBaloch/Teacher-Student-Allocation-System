import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { LanguageSwitcher } from "@/components/common/LanguageSwitcher";
import { languages } from "@/lib/i18n";

type MockTranslation = {
  i18n: { language: string };
  t: (key: string) => string;
};

vi.mock("react-i18next", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-i18next")>();
  return {
    ...actual,
    useTranslation: vi.fn(),
  };
});

vi.mock("@/lib/i18n", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@/lib/i18n")>();
  return {
    ...actual,
    default: {
      ...(actual.default as object),
      changeLanguage: vi.fn(),
    },
  };
});

import { useTranslation, type UseTranslationResponse } from "react-i18next";
import i18n from "@/lib/i18n";

const mockedUseTranslation = vi.mocked(useTranslation);
const mockedI18n = i18n as unknown as { changeLanguage: (code: string) => void };

describe("LanguageSwitcher", () => {
  beforeEach(() => {
    const mock: MockTranslation = {
      i18n: { language: "en" },
      t: (key: string) => key,
    };
    mockedUseTranslation.mockReturnValue(mock as unknown as UseTranslationResponse<string, undefined>);
    vi.clearAllMocks();
  });

  it("renders a button that opens the language menu", async () => {
    const user = userEvent.setup();

    render(<LanguageSwitcher />);

    const trigger = screen.getByRole("button", { name: /change language/i });
    await user.click(trigger);

    // All configured languages should appear
    for (const lang of languages) {
      expect(screen.getByText(`languages.${lang.code}`)).toBeInTheDocument();
    }
  });

  it("calls i18n.changeLanguage when an option is clicked", async () => {
    const user = userEvent.setup();

    render(<LanguageSwitcher />);

    const trigger = screen.getByRole("button", { name: /change language/i });
    await user.click(trigger);

    const target = languages[1]; // e.g. "de"
    await user.click(screen.getByText(`languages.${target.code}`));

    expect(mockedI18n.changeLanguage).toHaveBeenCalledWith(target.code);
  });

  it("shows a check icon for the active language", async () => {
    const user = userEvent.setup();
    const activeCode = languages[0].code;

    const mock: MockTranslation = {
      i18n: { language: activeCode },
      t: (key: string) => key,
    };
    mockedUseTranslation.mockReturnValue(mock as unknown as UseTranslationResponse<string, undefined>);

    render(<LanguageSwitcher />);

    const trigger = screen.getByRole("button", { name: /change language/i });
    await user.click(trigger);

    const activeItem = screen.getByText(`languages.${activeCode}`).closest("div");
    expect(activeItem).toBeTruthy();
  });
});

