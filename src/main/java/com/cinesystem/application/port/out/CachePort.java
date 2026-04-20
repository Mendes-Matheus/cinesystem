package com.cinesystem.application.port.out;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

    <T> Optional<T> get(String key, TypeReference<T> type);

    void set(String key, Object value, Duration ttl);

    void evictByPrefix(String prefix);
}