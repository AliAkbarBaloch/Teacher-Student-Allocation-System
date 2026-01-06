import {
    SCHOOL_TYPE_VALUES, 
    type SchoolType,
} from "@/features/schools/types/school.types";

import type { InternshipDemandDto, InternshipDemand} from "@/features/internship-demand/types";


//type guard : checks if a string a valid SchoolType
export function isSchoolType(value: string): value is SchoolType {
    return SCHOOL_TYPE_VALUES.includes(value as SchoolType);
}

//mapper : DTO => safe frontend object 
export function mapInternshipDemandDto(dto: InternshipDemandDto) : InternshipDemand | null {
    if (!isSchoolType(dto.schoolType)) {
        return null;
    }

    return {
        ...dto,
        schoolType: dto.schoolType,
        
    };
}
//mapper for lists 
export function mapInternshipDemandList(dtos: InternshipDemandDto[]) : InternshipDemand[] {
    return dtos
        .map(mapInternshipDemandDto)
        .filter((x): x is InternshipDemand => x !== null);
}