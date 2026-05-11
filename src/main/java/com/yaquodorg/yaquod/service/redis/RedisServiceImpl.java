package com.yaquodorg.yaquod.service.redis;

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

    @Override
    public void setValue(String key, String value, long ttlSeconds) {
        try {
            Boolean isNew =
                    redisTemplate
                            .opsForValue()
                            .setIfAbsent(key, value, Duration.ofSeconds(ttlSeconds));
            if (Boolean.FALSE.equals(isNew)) {
                log.warn("Key {} already exists in Redis. Overwriting with new value.", key);
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            } else {
                log.info("Key {} set to value: {} with TTL duration of {}", key, value, ttlSeconds);
            }
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), log warning and continue
            log.warn("Redis is unavailable for setting value, skipping cache operation", e);
        }
    }

    @Override
    public String getValue(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.info("Existing key found: {}", key);
            }
            return value;
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), return null
            log.warn("Redis is unavailable for key lookup, assuming new request", e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            // Redis is unavailable (e.g., in tests), log warning and continue
            log.warn("Redis is unavailable for invalidation, skipping", e);
        }
    }
}
