package de.unipassau.allocationsystem.mapper;

import java.util.List;


public interface BaseMapper<ENT, CREATE_DTO, UPDATE_DTO, RESPONSE_DTO> {
    ENT toEntityCreate(CREATE_DTO createDto);
    ENT toEntityUpdate(UPDATE_DTO createDto);

    RESPONSE_DTO toResponseDto(ENT entity);
    List<RESPONSE_DTO> toResponseDtoList(List<ENT> entities);
    void updateEntityFromDto(UPDATE_DTO updateDto, ENT entity);
}
