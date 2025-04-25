package com.microservice.accreditations.testUtils;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;
import com.microservice.accreditations.enums.CacheType;
import com.microservice.accreditations.utils.AccreditationCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

class AccreditationCacheTest {

    private AccreditationCache accreditationCache;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOps;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        accreditationCache = new AccreditationCache(redisTemplate);
    }

    @Test
    void testGetCachedAccreditationsReturnsData() {
        // Create sample data
        AccreditationResponseDTO dto = new AccreditationResponseDTO(
                1001L,
                5L,
                1500.00,
                2L,
                "Sucursal Centro",
                LocalDateTime.of(2024, 7, 10, 14, 30)
        );
        List<AccreditationResponseDTO> expectedAccreditations = List.of(dto);
        when(valueOps.get(CacheType.ACCREDITATIONS.getValues())).thenReturn(expectedAccreditations);

        List<AccreditationResponseDTO> result = accreditationCache.getCachedAccreditations();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void testGetCachedAccreditationsReturnsNullWhenNoData() {
        when(valueOps.get(CacheType.ACCREDITATIONS.getValues())).thenReturn(null);
        List<AccreditationResponseDTO> result = accreditationCache.getCachedAccreditations();
        assertNull(result);
    }

    @Test
    void testCacheAccreditations() {
        // Create sample data
        AccreditationResponseDTO dto = new AccreditationResponseDTO(
                1001L,
                5L,
                1500.00,
                2L,
                "Sucursal Centro",
                LocalDateTime.of(2024, 7, 10, 14, 30)
        );
        List<AccreditationResponseDTO> accreditations = List.of(dto);
        accreditationCache.cacheAccreditations(accreditations);

        verify(valueOps, times(1)).set(
                eq(CacheType.ACCREDITATIONS.getValues()),
                eq(accreditations),
                eq(Duration.ofMinutes(10))
        );
    }

    @Test
    void testClearCache() {
        when(valueOps.getOperations()).thenReturn(redisTemplate);
        accreditationCache.clearCache();
        verify(redisTemplate, times(1)).delete(CacheType.ACCREDITATIONS.getValues());
    }
}
