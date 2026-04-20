package com.cinesystem.infrastructure.cache;

import com.cinesystem.application.port.out.CachePort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Component
public class RedisCacheAdapter implements CachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheAdapter.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisCacheAdapter(StringRedisTemplate redisTemplate, ObjectMapper cacheObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = cacheObjectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception e) {
            log.warn("Cache corrompido para key '{}', removendo entrada. Causa: {}", key, e.getMessage());
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            log.error("Erro ao serializar valor para cache key '{}': {}", key, e.getMessage());
        }
    }

    @Override
    public void evictByPrefix(String keyPrefix) {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}