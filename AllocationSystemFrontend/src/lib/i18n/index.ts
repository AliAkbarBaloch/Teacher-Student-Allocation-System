import i18n from "i18next"; // core internationalization library 
import { initReactI18next } from "react-i18next"; // plugin which bridges i18next and React 
import LanguageDetector from "i18next-browser-languagedetector"; // adds auto language detection 

// Load translation files 
import enCommon from "./locales/en/common.json";
import enAuth from "./locales/en/auth.json";
import enHome from "./locales/en/home.json";
import enSettings from "./locales/en/settings.json";
import enRoles from "./locales/en/roles.json";
import enAuditLogs from "./locales/en/auditLogs.json";
import enSchools from "./locales/en/schools.json";
import enTeachers from "./locales/en/teachers.json";
import enInternshipTypes from "./locales/en/internshipTypes.json";
import enSubjects from "./locales/en/subjects.json";
import enSubjectCategories from "./locales/en/subjectCategories.json";
import enTeacherFormSubmissions from "./locales/en/teacherFormSubmissions.json";
import enAcademicYears from "./locales/en/academicYears.json";
import enTeacherSubjects from "./locales/en/teacherSubjects.json"

import deCommon from "./locales/de/common.json";
import deAuth from "./locales/de/auth.json";
import deHome from "./locales/de/home.json";
import deSettings from "./locales/de/settings.json";
import deRoles from "./locales/de/roles.json";
import deAuditLogs from "./locales/de/auditLogs.json";
import deSchools from "./locales/de/schools.json";
import deTeachers from "./locales/de/teachers.json";
import deInternshipTypes from "./locales/de/internshipTypes.json";
import deSubjects from "./locales/de/subjects.json";
import deSubjectCategories from "./locales/de/subjectCategories.json";
import deTeacherFormSubmissions from "./locales/de/teacherFormSubmissions.json";
import deAcademicYears from "./locales/de/academicYears.json";
import deTeacherSubjects from "./locales/de/teacherSubjects.json"

// Language metadata configuration
export const languages = [
    {
        code: "en",
        flag: "🇬🇧",
    },
    {
        code: "de",
        flag: "🇩🇪",
    },
] as const;

// Build the resource object 

export const resources = {
    en: {
        common: enCommon,
        auth: enAuth,
        home: enHome,
        settings: enSettings,
        roles: enRoles,
        auditLogs: enAuditLogs,
        schools: enSchools,
        teachers: enTeachers,
        internshipTypes: enInternshipTypes,
        subjects: enSubjects,
        subjectCategories: enSubjectCategories,
        teacherFormSubmissions: enTeacherFormSubmissions,
        academicYears: enAcademicYears,
        teacherSubjects: enTeacherSubjects,
    },
    de: {
        common: deCommon,
        auth: deAuth,
        home: deHome,
        settings: deSettings,
        roles: deRoles,
        auditLogs: deAuditLogs,
        schools: deSchools,
        teachers: deTeachers,
        internshipTypes: deInternshipTypes,
        subjects: deSubjects,
        subjectCategories: deSubjectCategories,
        teacherFormSubmissions: deTeacherFormSubmissions,
        academicYears: deAcademicYears,
        teacherSubjects: deTeacherSubjects,
    }
} as const;

// Supported language codes
export const supportedLanguages = languages.map((lang) => lang.code);

// Configure i18next 

i18n
.use(LanguageDetector) // use browser language detector 
.use(initReactI18next) // plug i18n into React 

// Call init with options 
.init(
    {
        resources, // all translations we just defined
        fallbackLng: "en", // if language detection fails, use english
        supportedLngs: supportedLanguages,
        defaultNS: "common", // default namespace 
        ns: ["common", "auth", "home", "settings", "roles", "auditLogs", "schools", "teachers", "internshipTypes", "subjects", "subjectCategories", "teacherFormSubmissions"], // list of namespaces we use 
        detection:{ // how to detect language 
            order:["querystring", "localStorage","navigator","htmlTag"],
            lookupQuerystring: "lang",
            caches: ["localStorage"] // remember chosen language in localStorage 
        },
        interpolation: { // how to handle value replacement 
        escapeValue: false // React already escapes values, so no double escaping 
        }, 
        returnNull:false // if a key is missing, show the key instead of null
    }
);

//export i18n instance 
export default i18n;



