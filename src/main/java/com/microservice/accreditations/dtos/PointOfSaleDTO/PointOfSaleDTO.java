package com.microservice.accreditations.dtos.PointOfSaleDTO;

import io.swagger.v3.oas.annotations.media.Schema;

public record PointOfSaleDTO(
        @Schema(description = "Unique identifier of the Point of Sale", example = "1")
        Long id,

        @Schema(description = "Name of the Point of Sale", example = "Terminal Norte")
        String name,

        @Schema(description = "Indicates whether the Point of Sale is active", example = "true")
        boolean active
) {}