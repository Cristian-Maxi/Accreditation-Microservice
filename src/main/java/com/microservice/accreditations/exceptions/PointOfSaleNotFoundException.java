package com.microservice.accreditations.exceptions;

public class PointOfSaleNotFoundException extends RuntimeException {
    public PointOfSaleNotFoundException(String message) {
        super(message);
    }
}