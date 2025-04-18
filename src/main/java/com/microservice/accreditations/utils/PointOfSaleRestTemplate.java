package com.microservice.accreditations.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.accreditations.dtos.PointOfSaleDTO.PointOfSaleDTO;
import com.microservice.accreditations.enums.CacheType;
import com.microservice.accreditations.exceptions.ExternalServiceException;
import com.microservice.accreditations.exceptions.PointOfSaleNotFoundException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PointOfSaleRestTemplate {

    private final RestTemplate restTemplate;
    private final HashOperations<String, String, Object> pointOfSaleCache;
    private final ObjectMapper objectMapper;

    public PointOfSaleRestTemplate(RestTemplate restTemplate, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.pointOfSaleCache = redisTemplate.opsForHash();
        this.objectMapper = objectMapper;
    }

    public PointOfSaleDTO getPointOfSaleFromCacheOrHttp(Long id) {
        String key = id.toString();
        Object cached = pointOfSaleCache.get(CacheType.POINT_OF_SALE.getValues(), key);

        if (cached != null) {
            if (cached instanceof PointOfSaleDTO) {
                return (PointOfSaleDTO) cached;
            } else if (cached instanceof Map) {
                return objectMapper.convertValue(cached, PointOfSaleDTO.class);
            }
        }

        try {
            ResponseEntity<PointOfSaleDTO> response = restTemplate.getForEntity(
                    "http://pointsalecost/api/pointOfSale/internal/" + id, PointOfSaleDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PointOfSaleDTO fetched = response.getBody();
                if (Boolean.TRUE.equals(fetched.active())) {
                    pointOfSaleCache.put(CacheType.POINT_OF_SALE.getValues(), key, fetched);
                }
                return fetched;
            }

        } catch (HttpClientErrorException.NotFound e) {
            throw new PointOfSaleNotFoundException("Point of sale not found.");
        } catch (Exception e) {
            throw new ExternalServiceException("Error fetching point of sale from external service.");
        }

        return null;
    }
}