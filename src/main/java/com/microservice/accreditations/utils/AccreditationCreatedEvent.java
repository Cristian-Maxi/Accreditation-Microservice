package com.microservice.accreditations.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccreditationCreatedEvent {
    private Long id;
    private String email;
    private Double amount;
    private Long pointOfSaleId;
    private String pointOfSaleName;
    private LocalDateTime receivedAt;
}