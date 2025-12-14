// What backend returns for a row (DTO)
export interface InternshipDemandDto {
    id: number;
    academicYearId: number;
    subjectId: number;
    internshipType: string;
    schoolType: string;
    requiredTeachers: number;
    studentCount: number;
    isForecasted: boolean;
    updatedAt: string;
}

// What we send to backend
export type CreateInternshipDemandRequest = {
    academicYearId: number;
    subjectId: number;
    internshipType: string;
    schoolType: string;
    requiredTeachers: number;
    studentCount: number;
    isForecasted: boolean;
};


// Your form state (now uses ids)
export interface DemandFormState {
    academicYearId: number | "";
    subjectId: number | "";
    internshipType: string;
    schoolType: string;
    requiredTeachers: number | "";
    studentCount: number | "";
    isForecasted: boolean;
}

// Filters can stay how you want; but if backend filters by year,
// you may want academicYearId instead of year later.
export interface DemandFilter {
    academicYearId: number | "";
    internshipType?: string;
    schoolType?: string;
    subjectId?: string;
    onlyForecasted?: boolean;
}
