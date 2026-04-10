package com.cinesystem.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {

    <T> Optional<T> get(String key);

    void set(String key, Object value, Duration ttl);

    void evictByPrefix(String prefix);


}