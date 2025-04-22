package com.microservice.accreditations.testDTOS;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;


import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccreditationRequestDTOTest {

    private final Validator validator;

    public AccreditationRequestDTOTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidDTO() {
        AccreditationRequestDTO dto = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, 3L);
        Set<ConstraintViolation<AccreditationRequestDTO>> violations = validator.validate(dto);
        assertEquals(0, violations.size());
    }

    @Test
    public void testInvalidEmail() {
        AccreditationRequestDTO dto = new AccreditationRequestDTO("", 250.75, 3L);
        Set<ConstraintViolation<AccreditationRequestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidAmount() {
        AccreditationRequestDTO dto = new AccreditationRequestDTO("cliente@ejemplo.com", null, 3L);
        Set<ConstraintViolation<AccreditationRequestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidPointOfSaleId() {
        AccreditationRequestDTO dto = new AccreditationRequestDTO("cliente@ejemplo.com", 250.75, null);
        Set<ConstraintViolation<AccreditationRequestDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
    }
}
