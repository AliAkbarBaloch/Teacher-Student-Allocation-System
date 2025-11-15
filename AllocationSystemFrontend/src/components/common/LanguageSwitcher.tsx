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
        {languages.map((language) => {
          const isActive = currentLanguageCode === language.code;
          const languageName = t(`languages.${language.code}`);
          
          return (
            <DropdownMenuItem
              key={language.code}
              onClick={() => changeLanguage(language.code)}
              className="cursor-pointer"
            >
              <span className="mr-2 text-lg" role="img" aria-label={languageName}>
                {language.flag}
              </span>
              <span>{languageName}</span>
              {isActive && (
                <Check className="ml-auto h-4 w-4" />
              )}
            </DropdownMenuItem>
          );
        })}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

