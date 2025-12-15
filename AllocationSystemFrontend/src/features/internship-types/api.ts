/* eslint-disable @typescript-eslint/no-explicit-any */
import type { InternshipType } from "./types/internshipType.types";

const API_BASE = import.meta.env.VITE_API_BASE_URL;

function getAuthHeader(): HeadersInit {
    const token = localStorage.getItem("auth_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function fetchInternshipTypes(): Promise<InternshipType[]> {
    const res = await fetch(`${API_BASE}/internship-types?includeRelations=true`, {
        headers: {
            "Content-Type": "application/json",
            ...getAuthHeader(),
        },
    });

    if (!res.ok) throw new Error(await res.text());

    const json = await res.json();

    const list =
        Array.isArray(json) ? json :
            Array.isArray((json as any)?.data) ? (json as any).data :
                Array.isArray((json as any)?.content) ? (json as any).content :
                    [];

    return list as InternshipType[];
}

export type { InternshipType };
