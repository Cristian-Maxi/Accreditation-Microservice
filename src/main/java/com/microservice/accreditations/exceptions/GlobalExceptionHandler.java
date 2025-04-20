package com.microservice.accreditations.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        logger.error("Validation failed: {}", errores);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errores);
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        logger.error("Entity not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Error in JSON format: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Error in JSON format");
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        logger.error("Validation exception: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        String message = ex.getCampo() != null ? ex.getCampo() + ": " + ex.getMessage() : ex.getMessage();
        logger.error("Application Exception: Field={}, Message={}", ex.getCampo(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT, message);
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(PointOfSaleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePointOfSaleNotFoundException(PointOfSaleNotFoundException ex) {
        logger.error("Point of Sale Not Found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUSerNotFoundException(PointOfSaleNotFoundException ex) {
        logger.error("User Not Found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(PointOfSaleInactiveException.class)
    public ResponseEntity<ErrorResponse> handlePointOfSaleInactiveException(PointOfSaleInactiveException ex) {
        logger.error("Point of Sale inactive: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(AccreditationSaveException.class)
    public ResponseEntity<ErrorResponse> handleAccreditationSaveException(AccreditationSaveException ex) {
        logger.error("Error saving accreditation: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(ExternalServiceException ex) {
        logger.error("Error in external service: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(HttpClientErrorException ex) {
        logger.error("HTTP Client Error: Code={}, Text={}", ex.getStatusCode(), ex.getStatusText());

        ErrorResponse errorResponse = new ErrorResponse((HttpStatus) ex.getStatusCode(), ex.getStatusText());
        return new ResponseEntity<>(errorResponse, errorResponse.status());
    }

    public record ErrorResponse(HttpStatus status, String message, List<String> errors) {
        public ErrorResponse(HttpStatus status, String message) {
            this(status, message, null);
        }
    }
}
