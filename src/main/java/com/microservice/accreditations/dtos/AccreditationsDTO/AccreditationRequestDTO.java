package com.microservice.accreditations.dtos.AccreditationsDTO;

import jakarta.validation.constraints.*;

public record AccreditationRequestDTO(
        @NotBlank(message = "Email must not be null")
        String email,
        @NotNull(message = "amount must not be null")
        Double amount,
        @NotNull(message = "pontOfSale ID must not be null")
        Long pointOfSaleId
) {}