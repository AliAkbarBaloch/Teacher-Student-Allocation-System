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
export function mapInternshipDemandDto(dto: InternshipDemandDto) : InternshipDemand {
    if (!isSchoolType(dto.schoolType)) {
        throw new Error(
            `Invalid schoolType recieved from backend: ${dto.schoolType}`

        );
    }

    return {
        ...dto,
        schoolType: dto.schoolType,
        
    };
}
//mapper for lists 
export function mapInternshipDemandList(dtos: InternshipDemandDto[]) : InternshipDemand[] {
    return dtos.map(mapInternshipDemandDto);
}