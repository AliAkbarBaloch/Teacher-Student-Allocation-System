/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { INTERNSHIP_DEMAND_BASE_URL} from "@/config.ts";
import type {
    InternshipDemandDto as InternshipDemand, 
    DemandFilter, 
    CreateInternshipDemandRequest,
} from "./types.ts";

//Base path for the backend endpoints 

//const BASE_URL = INTERNSHIP_DEMAND_BASE_URL;

function getAuthHeader(): HeadersInit {
    const token = localStorage.getItem("auth_token");
    return token ? {Authorization: `Bearer ${token}` } : {};
}

//turn filter object into a query string like 
//?year=2025&subject=Math 
function buildQuery(filter: DemandFilter)
{
    // URLSearchParams build-in browser helper to build query strings 
    const params = new URLSearchParams();

    // year is not an empty string and year is not null or undefined 
    if (filter.academicYearId !== "" && filter.academicYearId != null )
    {
        // add a parameter year with the value converted to string 
        params.set("academicYearId", String(filter.academicYearId));
    }

    if (filter.subjectId !== "" && filter.subjectId != null )
    {
        params.set("subjectId", String(filter.subjectId));
    }
        
    if (filter.internshipTypeId) params.set("internshipTypeId", String(filter.internshipTypeId));
    
    if (filter.schoolType) params.set("schoolType", filter.schoolType);

    if(filter.onlyForecasted) params.set("onlyForecasted", "true");

    //turns all parameters in one string 
    const qs = params.toString();

    // if qs is not empty - the string "?year2025&subject=Math" or empty string 
    return qs ? `?${qs}` : "";

}

// Helper function to handle backend responses and throw errors 
// async - this function uses await and returns a Promise 
// res : Response - parameter res in an HTTP Response object (from fetch)
async function handleResponse(res: Response)
{
    // 200-299 - success 
    if (res.ok) 
    {
        if (res.status === 204) return null; // No content 
        return res.json(); // json parses the response body as JSON and returns the data 
    }

    // if res.ok is false 

    // error message 
    let msg = "Result failed";

    try 
    {
        //try to read the error body as JSON 
        const body = await res.json();
        //if body has message field, use this field as error text 
        // else convert the whole body object to a string 
        msg = (body as any).message || JSON.stringify(body);

    }
    catch 
    {
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
export async function fetchInternshipDemand(filter: DemandFilter): Promise<InternshipDemand[]>
{

    // fetch - is a build in browser function to make HTTP requests 
    // URL being called: 
    // `${BASE_URL}${buildQuery(filter)}`:
    // ${BASE_URL} → /api/internship-demand
    // ${buildQuery(filter)} → something like "?year=2025&subject=Math" or ""
    // Together → /api/internship-demand?year=2025&subject=Math.

    // call the backend URL with the selected filters, and include cookies 
    // await means wait until the response comes back and store it in res 
    const res = await fetch(`${INTERNSHIP_DEMAND_BASE_URL}${buildQuery(filter)}`, {
        headers:{
        ...getAuthHeader(), 
        },
        });

    const json = await handleResponse(res);

    //normalize to array 
    if (Array.isArray(json)) return json; 

    if (json && Array.isArray((json as any).content)) return (json as any).content;
    if (json && Array.isArray((json as any).items)) return (json as any).items;
    if (json && Array.isArray((json as any).data)) return (json as any).data;

    //fallback 
    return [];

}



// Create a new internship demand entry 
// input payload : CreateDemandPayload 
// return - one intershipDemand object (the created row as the backend sends it back)
export async function createInternshipDemand(payload: CreateInternshipDemandRequest): Promise <InternshipDemand>
{

    //call fetch with 
    // URL = BASE_URL = /internship-demand 
    // method - POST - HTTP verb for " creating "
    // headers: tells backend we are sending JSON 
    // credentials - send cookies 
    // body - convert the payload object into JSON string 
    const res = await fetch(INTERNSHIP_DEMAND_BASE_URL, 
        {
            method: "POST",
            headers: {
                "Content-Type" : "application/json",
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
    payload: Partial <CreateInternshipDemandRequest>
): Promise<InternshipDemand>
{
    // calls eg /internship-demand/123 
    // PUT - HTTP verb for "update/replace"
    const res = await fetch(`${INTERNSHIP_DEMAND_BASE_URL}/${id}`, 
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
export async function deleteInternshipDemand(id: string) : Promise<void> {

    // no body, just the method and cookies 
    const res = await fetch(`${INTERNSHIP_DEMAND_BASE_URL}/${id}`,
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