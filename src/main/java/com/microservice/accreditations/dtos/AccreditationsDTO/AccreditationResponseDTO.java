package com.microservice.accreditations.dtos.AccreditationsDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AccreditationResponseDTO(
        @Schema(description = "Unique identifier of the accreditation", example = "1001")
        Long id,

        @Schema(description = "ID of the user who made the accreditation", example = "5")
        Long userId,

        @Schema(description = "Amount accredited", example = "1500.00")
        Double amount,

        @Schema(description = "ID of the Point of Sale where the accreditation was made", example = "2")
        Long pointOfSaleId,

        @Schema(description = "Name of the Point of Sale", example = "Sucursal Centro")
        String pointOfSaleName,

        @Schema(description = "Date and time when the accreditation was received", example = "2024-07-10T14:30:00")
        LocalDateTime receivedAt
) {}