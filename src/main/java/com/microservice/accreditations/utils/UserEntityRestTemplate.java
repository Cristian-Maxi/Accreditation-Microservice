package com.microservice.accreditations.utils;

import com.microservice.accreditations.exceptions.ExternalServiceException;
import com.microservice.accreditations.exceptions.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class UserEntityRestTemplate {

    private final RestTemplate restTemplate;

    public UserEntityRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Long validateUserEmail(String email) {
        try {
            ResponseEntity<Long> response = restTemplate.getForEntity(
                    "http://user/api/user/validateUserByEmail/" + email, Long.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new UserNotFoundException("User not found.");
        } catch (HttpClientErrorException e) {
            throw new ExternalServiceException("Error fetching user from external service.");
        } catch (Exception e) {
            throw new ExternalServiceException("Error fetching user from external service.");
        }
        return null;
    }
}