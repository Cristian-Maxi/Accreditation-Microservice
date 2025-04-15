package com.microservice.accreditations.dtos.AccreditationsDTO;

import jakarta.validation.constraints.NotNull;

public record AccreditationRequestDTO(
        @NotNull(message = "amount must not be null")
        Double amount,
        @NotNull(message = "pontOfSale ID must not be null")
        Long pointOfSaleId
) {}