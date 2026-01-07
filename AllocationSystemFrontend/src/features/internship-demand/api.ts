/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { INTERNSHIP_DEMAND_LIST_URL } from "@/config.ts";
import {INTERNSHIP_DEMAND_CRUD_URL } from "@/config.ts";
import type {
    InternshipDemandDto,
    InternshipDemand,
    DemandFilter,
    CreateInternshipDemandRequest,
} from "./types.ts";

import { mapInternshipDemandList } from "./mappers/internshipDemand.mapper.ts";

//Base path for the backend endpoints 

//const BASE_URL = INTERNSHIP_DEMAND_BASE_URL;

function getAuthHeader(): HeadersInit {
    const token = localStorage.getItem("auth_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
}

//turn filter object into a query string like 
//?year=2025&subject=Math 

function buildQuery(filter: DemandFilter) {

    const params = new URLSearchParams();

    // academic year is REQUIRED for this endpoint
    if (filter.academicYearId == null) {
        return "";
    }
    params.set("academic_year_id", String(filter.academicYearId));

    if (filter.subjectId != null) {
        params.set("subject_id", String(filter.subjectId));
    }
    if (filter.internshipTypeId != null) {
        params.set("internship_type_id", String(filter.internshipTypeId));
    }
    if (filter.schoolType != null) {
        params.set("school_type", filter.schoolType);
    }
    if (filter.onlyForecasted) {
        params.set("is_forecasted", "true");
    }

    const qs = params.toString();
    return qs ? `?${qs}` : "";

}


// Helper function to handle backend responses and throw errors 
// async - this function uses await and returns a Promise 
// res : Response - parameter res in an HTTP Response object (from fetch)
async function handleResponse(res: Response) {
    // 200-299 - success 
    if (res.ok) {
        if (res.status === 204) return null; // No content 
        return res.json(); // json parses the response body as JSON and returns the data 
    }

    // if res.ok is false 

    // error message 
    let msg = "Result failed";

    try {
        //try to read the error body as JSON 
        const body = await res.json();
        //if body has message field, use this field as error text 
        // else convert the whole body object to a string 
        msg = (body as any).message || JSON.stringify(body);

    }
    catch {
        //try to read body as raw text instead 
        msg = await res.text();

    }
    // this makes the function fail and passess the error up tp the caller
    // the caller (your UI code) can catch this and show the message 
    // 
    throw new Error(msg || `Request failed with status ${res.status}`);

}

// load internship demand list using filters 
// async function fetchInternshipDemand - defines an asynchronous function 
// input - filter : DemandFilter - an object describing filter options 
// output - eventually gives you a list (array) of InternshipDemand objects.
export async function fetchInternshipDemand(filter: DemandFilter): Promise<InternshipDemand[]> {

    // fetch - is a build in browser function to make HTTP requests 
    // URL being called: 
    // `${BASE_URL}${buildQuery(filter)}`:
    // ${BASE_URL} → /api/internship-demand
    // ${buildQuery(filter)} → something like "?year=2025&subject=Math" or ""
    // Together → /api/internship-demand?year=2025&subject=Math.


    const url = `${INTERNSHIP_DEMAND_LIST_URL}${buildQuery(filter)}`;

    console.log("Fetch internship demand ", url);

    // call the backend URL with the selected filters, and include cookies 
    // await means wait until the response comes back and store it in res 
    const res = await fetch(url, {
        headers: {
            ...getAuthHeader(),
        },
    });

    const json = await handleResponse(res);


    // IMPORTANT: list-filter returns ResponseHandler.success + Page
    const list =
    Array.isArray(json) ? json :
    Array.isArray((json as any)?.content) ? (json as any).content :
    Array.isArray((json as any)?.data?.content) ? (json as any).data.content :
    Array.isArray((json as any)?.data?.items) ? (json as any).data.items :
    [];


    console.log("FETCH URL", url);
    console.log("EXTRACTED rows", list.length);

    //validate / convert schoolType here 
    return mapInternshipDemandList(list);

}



// Create a new internship demand entry 
// input payload : CreateDemandPayload 
// return - one intershipDemand object (the created row as the backend sends it back)
export async function createInternshipDemand(payload: CreateInternshipDemandRequest): Promise<InternshipDemand> {

    //call fetch with 
    // URL = BASE_URL = /internship-demand 
    // method - POST - HTTP verb for " creating "
    // headers: tells backend we are sending JSON 
    // credentials - send cookies 
    // body - convert the payload object into JSON string 
    const res = await fetch(INTERNSHIP_DEMAND_CRUD_URL,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                ...getAuthHeader(),
            },
            body: JSON.stringify(payload),

        }
    );
    //let handleresponse parse success or throw error 
    return handleResponse(res);
}

// Update an existing internship demans entry 
// id - which record is updated 
// payload - Partial means some or all fields from CreateDemandPayload 
// return - updated Intershipdemand from backend 
export async function updateInternshipDemand(
    id: string,
    payload: Partial<CreateInternshipDemandRequest>
): Promise<InternshipDemand> {
    // calls eg /internship-demand/123 
    // PUT - HTTP verb for "update/replace"
    const res = await fetch(`${INTERNSHIP_DEMAND_CRUD_URL}/${id}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                ...getAuthHeader(),
            },
            body: JSON.stringify(payload),
        }
    );
    return handleResponse(res);

}

// delete an internship demand entry 
// id - record id 
// no data retutned - just success or error 
export async function deleteInternshipDemand(id: string): Promise<void> {

    // no body, just the method and cookies 
    const res = await fetch(`${INTERNSHIP_DEMAND_CRUD_URL}/${id}`,
        {
            method: "DELETE",
            headers: {
                ...getAuthHeader(),
            },
        }
    );

    // if backend respond with 204, 200, no error 
    // id error status, haldleResponse will throw an error 
    await handleResponse(res);


}