const API_BASE = import.meta.env.VITE_API_BASE_URL;

function getAuthHeader(): Record<string, string> {
    const token = localStorage.getItem("auth_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

import type { AcademicYear } from "./types/academicYear.types";

export async function fetchAcademicYears(): Promise<AcademicYear[]> {
    const res = await fetch(`${API_BASE}/academic-years?includeRelations=true`, {
        headers: { "Content-Type": "application/json", ...getAuthHeader() },
    });

    if (!res.ok) throw new Error(await res.text());

    const json = await res.json();
    console.log("fetchAcademicYears raw json:", json);

    return Array.isArray(json?.data) ? json.data : [];
}
