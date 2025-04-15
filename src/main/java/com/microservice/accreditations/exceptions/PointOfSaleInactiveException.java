package com.microservice.accreditations.exceptions;

public class PointOfSaleInactiveException extends RuntimeException {
    public PointOfSaleInactiveException(String message) {
        super(message);
    }
}
