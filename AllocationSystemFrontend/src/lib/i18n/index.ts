import i18n from "i18next"; // core internationalization library 
import LanguageDetector from "i18next-browser-languagedetector"; // adds auto language detection 
import { initReactI18next } from "react-i18next"; // plugin which bridges i18next and React 

// Load translation files 
import enUsers from "./locales/en/users.json";
import enAcademicYears from "./locales/en/academicYears.json";
import enAllocationPlans from "./locales/en/allocationPlans.json";
import enAuditLogs from "./locales/en/auditLogs.json";
import enAuth from "./locales/en/auth.json";
import enCommon from "./locales/en/common.json";
import enCreditHourTracking from "./locales/en/creditHourTracking.json";
import enHome from "./locales/en/home.json";
import enInternshipTypes from "./locales/en/internshipTypes.json";
import enPlanChangeLogs from "./locales/en/planChangeLogs.json";
import enReportAllocations from "./locales/en/reportAllocations.json";
import enReportPlanAnalytics from "./locales/en/reportPlanAnalytics.json";
import enReportSchools from "./locales/en/reportSchools.json";
import enReportTeachers from "./locales/en/reportTeachers.json";
import enRoles from "./locales/en/roles.json";
import enSchools from "./locales/en/schools.json";
import enSettings from "./locales/en/settings.json";
import enSubjectCategories from "./locales/en/subjectCategories.json";
import enSubjects from "./locales/en/subjects.json";
import enTeacherAssignments from "./locales/en/teacherAssignments.json";
import enTeacherAvailability from "./locales/en/teacherAvailability.json";
import enTeachers from "./locales/en/teachers.json";
import enTeacherSubjects from "./locales/en/teacherSubjects.json";
import enTeacherSubmissions from "./locales/en/teacherSubmissions.json";
import enZoneConstraints from "./locales/en/zoneConstraints.json";

import deUsers from "./locales/de/users.json";
import deAcademicYears from "./locales/de/academicYears.json";
import deAllocationPlans from "./locales/de/allocationPlans.json";
import deAuditLogs from "./locales/de/auditLogs.json";
import deAuth from "./locales/de/auth.json";
import deCommon from "./locales/de/common.json";
import deCreditHourTracking from "./locales/de/creditHourTracking.json";
import deHome from "./locales/de/home.json";
import deInternshipTypes from "./locales/de/internshipTypes.json";
import dePlanChangeLogs from "./locales/de/planChangeLogs.json";
import deReportAllocations from "./locales/de/reportAllocations.json";
import deReportPlanAnalytics from "./locales/de/reportPlanAnalytics.json";
import deReportSchools from "./locales/de/reportSchools.json";
import deReportTeachers from "./locales/de/reportTeachers.json";
import deRoles from "./locales/de/roles.json";
import deSchools from "./locales/de/schools.json";
import deSettings from "./locales/de/settings.json";
import deSubjectCategories from "./locales/de/subjectCategories.json";
import deSubjects from "./locales/de/subjects.json";
import deTeacherAssignments from "./locales/de/teacherAssignments.json";
import deTeacherAvailability from "./locales/de/teacherAvailability.json";
import deTeachers from "./locales/de/teachers.json";
import deTeacherSubjects from "./locales/de/teacherSubjects.json";
import deTeacherSubmissions from "./locales/de/teacherSubmissions.json";
import deZoneConstraints from "./locales/de/zoneConstraints.json";

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
        users: enUsers,
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
        teacherSubmissions: enTeacherSubmissions,
        academicYears: enAcademicYears,
        teacherSubjects: enTeacherSubjects,
        teacherAvailability: enTeacherAvailability,
        zoneConstraints: enZoneConstraints,
        allocationPlans: enAllocationPlans,
        teacherAssignments: enTeacherAssignments,
        planChangeLogs: enPlanChangeLogs,
        creditHourTracking: enCreditHourTracking,
        reportAllocations: enReportAllocations,
        reportPlanAnalytics: enReportPlanAnalytics,
        reportTeachers: enReportTeachers,
        reportSchools: enReportSchools,
    },
    de: {
        common: deCommon,
        users: deUsers,
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
        teacherSubmissions: deTeacherSubmissions,
        academicYears: deAcademicYears,
        teacherSubjects: deTeacherSubjects,
        teacherAvailability: deTeacherAvailability,
        zoneConstraints: deZoneConstraints,
        allocationPlans: deAllocationPlans,
        teacherAssignments: deTeacherAssignments,
        planChangeLogs: dePlanChangeLogs,
        creditHourTracking: deCreditHourTracking,
        reportAllocations: deReportAllocations,
        reportPlanAnalytics: deReportPlanAnalytics,
        reportTeachers: deReportTeachers,
        reportSchools: deReportSchools,
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
        ns: [
            "common","users", "auth", "home", "settings", 
            "roles", "auditLogs", "schools", "teachers", 
            "internshipTypes", "subjects", "subjectCategories", 
            "teacherSubmissions", "zoneConstraints", "teacherAssignments", 
            "planChangeLogs", "creditHourTracking", "reportAllocations", 
            "reportPlanAnalytics", "reportTeachers", "reportSchools"
        ], // list of namespaces we use 
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



