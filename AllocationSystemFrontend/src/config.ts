export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"; 

console.log("API_BASE_URL =", API_BASE_URL); 

if(!API_BASE_URL) {
    console.warn("VITE_API_BASE_URL is not set");
}

export const INTERNSHIP_DEMAND_BASE_URL = `${API_BASE_URL}/internship-demands/list-filter`;

export const INTERNSHIP_DEMAND_CRUD_URL = `${API_BASE_URL}/internship-demands`;

export const INTERNSHIP_DEMAND_LIST_URL = `${API_BASE_URL}/internship-demands/list-filter`;

