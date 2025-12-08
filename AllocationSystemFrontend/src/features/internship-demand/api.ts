import {
    InternshipDemand, 
    DemandFilter, 
    CreateDemandPayload,
} from "./types.ts";

//Base path for the backend endpoints 

const BASE_URL = `${import.meta.env.VITE_API_BASE_URL}intership-demands`;

//turn filter object into a query string like 
//?year=2025&subject=Math 
function buildQuery(filter: DemandFilter)
{
    // URLSearchParams build-in browser helper to build query strings 
    const params = new URLSearchParams();

    // year is not an empty string and year is not null or undefined 
    if (filter.year !== "" && filter.year != null )
    {
        // add a parameter year with the value converted to string 
        params.set("year", String(filter.year));
    }
        
    if (filter.internshipType) params.set("internshipType", filter.internshipType);
    
    if (filter.schoolType) params.set("schoolType", filter.schoolType);

    if(filter.subject) params.set("subject",filter.subject);

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
async function fetchInternshipDemand(filter: DemandFilter): Promise<InternshipDemand[]>
{

    // fetch - is a build in browser function to make HTTP requests 
    // URL being called: 
    // `${BASE_URL}${buildQuery(filter)}`:
    // ${BASE_URL} → /api/internship-demand
    // ${buildQuery(filter)} → something like "?year=2025&subject=Math" or ""
    // Together → /api/internship-demand?year=2025&subject=Math.

    // call the backend URL with the selected filters, and include cookies 
    // await means wait until the response comes back and store it in res 
    const res = await fetch(`${BASE_URL}${buildQuery(filter)}`, {
        credentials: "include", // send cookies (auth)
    });


    //pass the response to handleResponse 
    return handleResponse(res);

}

// Create a new internship demand entry 
// input payload : CreateDemandPayload 
// return - one intershipDemand object (the created row as the backend sends it back)
export async function createInternshipDemand(payload: CreateDemandPayload): Promise <InternshipDemand>
{

    //call fetch with 
    // URL = BASE_URL = /internship-demand 
    // method - POST - HTTP verb for " creating "
    // headers: tells backend we are sending JSON 
    // credentials - send cookies 
    // body - convert the payload object into JSON string 
    const res = await fetch(BASE_URL, 
        {
            method: "POST",
            headers: {"Content-Type" : "application/json"},
            credentials: "include",
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
    payload: Partial <CreateDemandPayload>
): Promise<InternshipDemand>
{
    // calls eg /internship-demand/123 
    // PUT - HTTP verb for "update/replace"
    const res = await fetch(`${BASE_URL}/{id}`, 
        {
        method: "PUT",
        headers: {"Content-Type": "application/json"},
        credentials: "include",
        body: JSON.stringify(payload),
    }
    );
    return handleResponse(res);

}

// delete an internship demand entry 
// id - record id 
// no data retutned - just success or error 
export async function deleteIntershipDemand(id: string) : Promise<void> {

    // no body, just the method and cookies 
    const res = await fetch(`${BASE_URL}/${id}`,
        {
            method: "DELETE",
            credentials: "include",
        }
    );

    // if backend respond with 204, 200, no error 
    // id error status, haldleResponse will throw an error 
    await handleResponse(res);


}