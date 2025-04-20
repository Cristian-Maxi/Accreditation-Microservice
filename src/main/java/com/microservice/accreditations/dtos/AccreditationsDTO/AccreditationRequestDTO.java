package com.microservice.accreditations.dtos.AccreditationsDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record AccreditationRequestDTO(
        @Schema(description = "Email of the user making the accreditation", example = "cliente@ejemplo.com")
        @NotBlank(message = "Email must not be null")
        String email,

        @Schema(description = "Amount to accredit", example = "250.75")
        @NotNull(message = "amount must not be null")
        Double amount,

        @Schema(description = "ID of the Point of Sale where the accreditation is made", example = "3")
        @NotNull(message = "pontOfSale ID must not be null")
        Long pointOfSaleId
) {}