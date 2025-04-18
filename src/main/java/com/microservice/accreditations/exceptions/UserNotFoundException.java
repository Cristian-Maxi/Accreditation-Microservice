package com.microservice.accreditations.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String message) {super(message);}
}
