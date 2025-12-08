// shape of data 
// export - make this visible for other files that imort it. interface - template for an object 

// one row data in the table 
export interface InternshipDemand {

    id: string; //unique ID from the backend 
    year: number; 
    internshipType: string;
    schoolType: string; 
    subject: string; 
    requiredTeachers: number; 
    studentCount: number; 
    forecasted: boolean; 
    updatedAt: string;

}

// for the filter bar on the page 

export interface DemandFilter 
{
    year: number | ""; //required but can be "" while typing 
    internshipType?: string ; // optional 
    schoolType?: string; //optional 
    subject?: string; // optional 
    onlyForecasted?: boolean; //optional 
}

//create-edit dialog form 
export interface DemandFormState
{
    year: number | "";
    internshipType: string;
    schoolType: string;
    subject: string; 
    requiredTeachers: number | "";
    studentCount: number | "";
    forecasted : boolean;
}

//this is what we send to backend when creating/updating 

export type CreateDemandPayload = Omit<

InternshipDemand,
"id" | "updateAt"

>;
