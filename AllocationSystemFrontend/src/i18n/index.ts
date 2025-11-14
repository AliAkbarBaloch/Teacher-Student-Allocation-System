import i18n from "i18next"; // core internationalization library 
import { initReactI18next } from "react-i18next"; // plugin which bridges i18next and React 
import LanguageDetector from "i18next-browser-languagedetector"; // adds auto language detection 

// Load translation files 
import enCommon from "./locales/en/common.json";

import deCommon from "./locales/de/common.json";

// Build the resourse object 

export const resources = {
    en: {common: enCommon},
    de: {common: deCommon}
} as const;

// Confiure i18next 

i18n
.use(LanguageDetector)
.use(initReactI18next)

// Call init with options 
.init(
    {
        resources,
        fallbackLng: "en",
        supportedLngs: ["en", "de"],
        defaultNS: "common",
        ns: ["common"],
        detection:{
            order:["querystring", "localStorage","navigator","htmlTag"],
            lookupQuerystring: "lang",
            caches: ["localStorage"]
        },
        interpolation: {escapeValue: false},
        returnNull:false
    }
);

//export i18n instance 
export default i18n;


