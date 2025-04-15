package com.microservice.accreditations.services;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationRequestDTO;
import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;

import java.util.List;

public interface AccreditationService {
    AccreditationResponseDTO saveAccreditation(AccreditationRequestDTO request);
    List<AccreditationResponseDTO> getAllAccreditations();
}