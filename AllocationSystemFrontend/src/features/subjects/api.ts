const API_BASE = import.meta.env.VITE_API_BASE_URL;

function getAuthHeader(): Record<string, string> {
    const token = localStorage.getItem("auth_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

import type { Subject } from "./types/subject.types";


export async function fetchSubjects() {
    const res = await fetch(`${API_BASE}/subjects?includeRelations=true`, {
        headers: {
            "Content-Type": "application/json",
            ...getAuthHeader(),
        },
    });

    if (!res.ok) throw new Error(await res.text());

    const json = await res.json();

    const list =
        Array.isArray(json) ? json :
            Array.isArray(json?.data) ? json.data :
                Array.isArray(json?.content) ? json.content :
                    Array.isArray(json?.items) ? json.items :
                        [];

    return list as Subject[];
}
