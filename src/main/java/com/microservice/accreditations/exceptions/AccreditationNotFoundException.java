package com.microservice.accreditations.exceptions;

public class AccreditationNotFoundException extends RuntimeException {
    public AccreditationNotFoundException(String message) {
        super(message);
    }
}
