package com.microservice.accreditations.testUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.accreditations.dtos.PointOfSaleDTO.PointOfSaleDTO;
import com.microservice.accreditations.enums.CacheType;
import com.microservice.accreditations.exceptions.ExternalServiceException;
import com.microservice.accreditations.exceptions.PointOfSaleNotFoundException;
import com.microservice.accreditations.utils.PointOfSaleRestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

class PointOfSaleRestTemplateTest {

    private PointOfSaleRestTemplate pointOfSaleRestTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        pointOfSaleRestTemplate = new PointOfSaleRestTemplate(restTemplate, redisTemplate, objectMapper);
    }

    @Test
    void testGetPointOfSaleFromCacheReturnsDTO() {
        Long id = 1L;
        PointOfSaleDTO cachedDTO = new PointOfSaleDTO(id, "Point A", true);

        when(hashOperations.get(CacheType.POINT_OF_SALE.getValues(), id.toString())).thenReturn(cachedDTO);

        PointOfSaleDTO result = pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(id);

        assertNotNull(result);
        assertEquals(cachedDTO, result);
    }

    @Test
    void testGetPointOfSaleFromCacheReturnsConvertedDTO() {
        Long id = 1L;
        Map<String, Object> cachedMap = Map.of("id", id, "name", "Point A", "active", true);
        PointOfSaleDTO convertedDTO = new PointOfSaleDTO(id, "Point A", true);

        when(hashOperations.get(CacheType.POINT_OF_SALE.getValues(), id.toString())).thenReturn(cachedMap);
        when(objectMapper.convertValue(cachedMap, PointOfSaleDTO.class)).thenReturn(convertedDTO);

        PointOfSaleDTO result = pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(id);

        assertNotNull(result);
        assertEquals(convertedDTO, result);
    }

    @Test
    void testGetPointOfSaleFromHttpReturnsDTOAndCachesIt() {
        Long id = 1L;
        PointOfSaleDTO fetchedDTO = new PointOfSaleDTO(id, "Point A", true);
        ResponseEntity<PointOfSaleDTO> responseEntity = new ResponseEntity<>(fetchedDTO, HttpStatus.OK);

        when(hashOperations.get(CacheType.POINT_OF_SALE.getValues(), id.toString())).thenReturn(null);
        when(restTemplate.getForEntity("http://pointsalecost/api/pointOfSale/internal/" + id, PointOfSaleDTO.class))
                .thenReturn(responseEntity);

        PointOfSaleDTO result = pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(id);

        assertNotNull(result);
        assertEquals(fetchedDTO, result);
        verify(hashOperations, times(1)).put(CacheType.POINT_OF_SALE.getValues(), id.toString(), fetchedDTO);
    }

    @Test
    void testGetPointOfSaleNotFoundException() {
        Long id = 1L;

        HttpClientErrorException exceptionToThrow =
                HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Point of Sale not found",
                        HttpHeaders.EMPTY,
                        null,
                        null
                );

        when(hashOperations.get(CacheType.POINT_OF_SALE.getValues(), id.toString())).thenReturn(null);
        when(restTemplate.getForEntity("http://pointsalecost/api/pointOfSale/internal/" + id, PointOfSaleDTO.class))
                .thenThrow(exceptionToThrow);

        PointOfSaleNotFoundException exception = assertThrows(PointOfSaleNotFoundException.class, () ->
                pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(id)
        );

        assertEquals("Point of sale not found.", exception.getMessage());
    }

    @Test
    void testGetPointOfSaleExternalServiceException() {
        Long id = 1L;

        when(hashOperations.get(CacheType.POINT_OF_SALE.getValues(), id.toString())).thenReturn(null);
        when(restTemplate.getForEntity("http://pointsalecost/api/pointOfSale/internal/" + id, PointOfSaleDTO.class))
                .thenThrow(new RuntimeException("Service unavailable"));

        ExternalServiceException exception = assertThrows(ExternalServiceException.class, () ->
                pointOfSaleRestTemplate.getPointOfSaleFromCacheOrHttp(id)
        );

        assertEquals("Error fetching point of sale from external service.", exception.getMessage());
    }
}