package com.microservice.accreditations.dtos.AccreditationsDTO;

import java.time.LocalDateTime;

public record AccreditationResponseDTO(
        Long id,
        Long userId,
        Double amount,
        Long pointOfSaleId,
        String pointOfSaleName,
        LocalDateTime receivedAt
) {}