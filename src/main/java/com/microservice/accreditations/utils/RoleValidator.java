package com.microservice.accreditations.utils;

import com.microservice.accreditations.enums.RoleEnum;
import com.microservice.accreditations.exceptions.AccessDeniedException;

public class RoleValidator {
    public static void validateRole(String rolesHeader, String errorMessage, RoleEnum... allowedRoles) {
        for (RoleEnum role : allowedRoles) {
            if (rolesHeader.contains(role.name())) return;
        }
        throw new AccessDeniedException(errorMessage);
    }
}