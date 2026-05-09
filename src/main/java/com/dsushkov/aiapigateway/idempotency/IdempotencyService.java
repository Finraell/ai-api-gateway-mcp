package com.dsushkov.aiapigateway.idempotency;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private static final Duration TTL = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, CachedValue> cache = new ConcurrentHashMap<>();

    public <T> T execute(String key, Supplier<T> supplier) {
        if (key == null || key.isBlank()) {
            return supplier.get();
        }
        evictExpired();
        CachedValue value = cache.computeIfAbsent(key, ignored -> new CachedValue(supplier.get(), Instant.now().plus(TTL)));
        @SuppressWarnings("unchecked")
        T typed = (T) value.value();
        return typed;
    }

    public Optional<Object> get(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        evictExpired();
        CachedValue value = cache.get(key);
        return value == null ? Optional.empty() : Optional.of(value.value());
    }

    private void evictExpired() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record CachedValue(Object value, Instant expiresAt) {
    }
}
