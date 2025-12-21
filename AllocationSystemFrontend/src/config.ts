export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL; 

console.log("API_BASE_URL =", API_BASE_URL); 

if(!API_BASE_URL) {
    console.warn("VITE_API_BASE_URL is not set");
}

export const INTERNSHIP_DEMAND_BASE_URL = `${API_BASE_URL}/internship-demands`;

