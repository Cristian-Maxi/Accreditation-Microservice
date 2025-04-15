package com.microservice.accreditations.utils;

import com.microservice.accreditations.dtos.AccreditationsDTO.AccreditationResponseDTO;
import com.microservice.accreditations.enums.CacheType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class AccreditationCache {

    private final ValueOperations<String, Object> valueOps;

    public AccreditationCache(RedisTemplate<String, Object> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    public List<AccreditationResponseDTO> getCachedAccreditations() {
        Object cached = valueOps.get(CacheType.ACCREDITATIONS.getValues());
        if (cached instanceof List<?>) {
            return (List<AccreditationResponseDTO>) cached;
        }
        return null;
    }

    public void cacheAccreditations(List<AccreditationResponseDTO> accreditations) {
        valueOps.set(CacheType.ACCREDITATIONS.getValues(), accreditations, Duration.ofMinutes(10)); // expira en 10 min
    }

    public void clearCache() {
        valueOps.getOperations().delete(CacheType.ACCREDITATIONS.getValues());
    }
}
