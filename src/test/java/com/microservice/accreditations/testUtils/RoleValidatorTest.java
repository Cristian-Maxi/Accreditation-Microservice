package com.microservice.accreditations.testUtils;

import com.microservice.accreditations.enums.RoleEnum;
import com.microservice.accreditations.exceptions.AccessDeniedException;
import com.microservice.accreditations.utils.RoleValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoleValidatorTest {

    private RoleValidator roleValidator;

    @Test
    void testValidateRole_WhenHeaderDoesNotContainAllowedRoles_ThrowsAccessDenied() {
        assertThrows(AccessDeniedException.class, () ->
                RoleValidator.validateRole("ROLE_CLIENT", "No access", RoleEnum.ADMIN));
    }
}
