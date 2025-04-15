package com.microservice.accreditations.mappers;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationRequestDTO;
import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;
import com.microservice.accreditations.models.Accreditation;
import com.microservice.accreditations.utils.AccreditationCreatedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AccreditationMapper {
    AccreditationMapper INSTANCE = Mappers.getMapper(AccreditationMapper.class);

    AccreditationResponseDTO toAccreditationResponseDTO(Accreditation accreditation);

    Accreditation toEntity(AccreditationRequestDTO accreditationRequestDTO);

    AccreditationCreatedEvent toAccreditationCreatedEvent(Accreditation accreditation);
}