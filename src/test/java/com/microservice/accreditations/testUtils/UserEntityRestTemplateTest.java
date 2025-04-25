package com.microservice.accreditations.testUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.microservice.accreditations.exceptions.ExternalServiceException;
import com.microservice.accreditations.exceptions.UserNotFoundException;
import com.microservice.accreditations.utils.UserEntityRestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class UserEntityRestTemplateTest {

    private UserEntityRestTemplate userEntityRestTemplate;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userEntityRestTemplate = new UserEntityRestTemplate(restTemplate);
    }

    @Test
    void testValidateUserEmailSuccess() {
        String email = "test@example.com";
        Long userId = 123L;
        ResponseEntity<Long> responseEntity = new ResponseEntity<>(userId, HttpStatus.OK);

        when(restTemplate.getForEntity("http://user/api/user/validateUserByEmail/" + email, Long.class))
                .thenReturn(responseEntity);
        Long result = userEntityRestTemplate.validateUserEmail(email);

        assertNotNull(result);
        assertEquals(userId, result);
    }

    @Test
    void testValidateUserEmailUserNotFound() {
        String email = "notfound@example.com";

        HttpClientErrorException exceptionToThrow =
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        HttpHeaders.EMPTY,
                        null,
                        null
                );

        when(restTemplate.getForEntity("http://user/api/user/validateUserByEmail/" + email, Long.class))
                .thenThrow(exceptionToThrow);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userEntityRestTemplate.validateUserEmail(email));

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testValidateUserEmailExternalServiceError() {
        String email = "error@example.com";

        when(restTemplate.getForEntity("http://user/api/user/validateUserByEmail/" + email, Long.class))
                .thenThrow(new RuntimeException("Service error"));
        ExternalServiceException exception = assertThrows(ExternalServiceException.class,
                () -> userEntityRestTemplate.validateUserEmail(email));

        assertEquals("Error fetching user from external service.", exception.getMessage());
    }

    @Test
    void testValidateUserEmailNullResponseBody() {
        String email = "nullbody@example.com";
        ResponseEntity<Long> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.getForEntity("http://user/api/user/validateUserByEmail/" + email, Long.class))
                .thenReturn(responseEntity);
        Long result = userEntityRestTemplate.validateUserEmail(email);

        assertNull(result);
    }
}
