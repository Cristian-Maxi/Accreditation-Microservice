package com.microservice.accreditations.exceptions;

public class AccreditationSaveException extends RuntimeException {
    public AccreditationSaveException(String message) {
        super(message);
    }
}
