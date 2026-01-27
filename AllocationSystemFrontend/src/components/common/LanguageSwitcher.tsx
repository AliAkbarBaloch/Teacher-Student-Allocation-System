import { Languages, Check } from "lucide-react";
import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import i18n, { languages } from "@/lib/i18n";

/**
 * Props for the LanguageItem component.
 */
interface LanguageItemProps {
  language: { code: string; flag: string };
  isActive: boolean;
  languageName: string;
  onClick: (code: string) => void;
}

/**
 * A sub-component for rendering a single language item in the dropdown.
 */
function LanguageItem({
  language,
  isActive,
  languageName,
  onClick,
}: LanguageItemProps) {
  return (
    <DropdownMenuItem
      onClick={() => onClick(language.code)}
      className="cursor-pointer"
    >
      <span className="mr-2 text-lg" aria-hidden="true">
        {language.flag}
      </span>
      <span>{languageName}</span>
      {isActive && <Check className="ml-auto h-4 w-4" />}
    </DropdownMenuItem>
  );
}

export function LanguageSwitcher() {
  const { i18n: i18nInstance, t } = useTranslation("common");
  const currentLanguageCode = i18nInstance.language;

  const changeLanguage = (languageCode: string) => {
    i18n.changeLanguage(languageCode);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="icon" className="relative">
          <Languages className="h-[1.2rem] w-[1.2rem]" />
          <span className="sr-only">Change language</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        {languages.map((language) => (
          <LanguageItem
            key={language.code}
            language={language}
            isActive={currentLanguageCode === language.code}
            languageName={t(`languages.${language.code}`)}
            onClick={changeLanguage}
          />
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

