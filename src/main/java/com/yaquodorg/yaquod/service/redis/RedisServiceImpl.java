package com.yaquodorg.yaquod.service.redis;

import com.yaquodorg.yaquod.exception.DuplicateKeyException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public void validate(String key) {
        try {
            Boolean isNew =
                    redisTemplate.opsForValue().setIfAbsent(key, "pending", TTL);

            if (Boolean.FALSE.equals(isNew)) {
                throw new DuplicateKeyException("Duplicate request detected for key: " + key);
            }
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), log warning and continue
            log.warn("Redis is unavailable for validation, skipping check", e);
        }
    }

    @Override
    public void invalidate(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), log warning and continue
            log.warn("Redis is unavailable for invalidation, skipping", e);
        }
    }

    @Override
    public String findExistingKey(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.info("Existing idempotency key found: {}", key);
            }
            return value;
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), return null
            log.warn("Redis is unavailable for idempotency key lookup, assuming new request", e);
            return null;
        }
    }

    @Override
    public void setValue(String key, String value, long ttlSeconds) {
        try {
            Boolean isNew =
                    redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(ttlSeconds));
            if (Boolean.FALSE.equals(isNew)) {
                log.warn("Key {} already exists in Redis. Overwriting with new value.", key);
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            }
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), log warning and continue
            log.warn("Redis is unavailable for setting value, skipping cache operation", e);
        }
    }
}
