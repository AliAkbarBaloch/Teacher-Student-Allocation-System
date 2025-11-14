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
.use(LanguageDetector) // use browser language detector 
.use(initReactI18next) // plug i18n into React 

// Call init with options 
.init(
    {
        resources, // all translations we just defined
        fallbackLng: "en", // if language detection fails, use english
        supportedLngs: ["en", "de"],
        defaultNS: "common", // default namespace 
        ns: ["common"], // list of namespaces we use 
        detection:{ // how to detect language 
            order:["querystring", "localStorage","navigator","htmlTag"],
            lookupQuerystring: "lang",
            caches: ["localStorage"] // remember chosen lanauge in localStorage 
        },
        interpolation: { // how to handle value replacement 
        escapeValue: false // React already escapes values, so no double escaping 
        }, 
        returnNull:false // if a key is missing, show the key instead of null
    }
);

//export i18n instance 
export default i18n;


