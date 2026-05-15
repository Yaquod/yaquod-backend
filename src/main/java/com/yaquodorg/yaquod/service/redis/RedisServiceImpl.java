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

    @Override
    public void setValue(String key, String value) {
        try {
            Boolean isNew =
                    redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMinutes(5));
            if (Boolean.FALSE.equals(isNew)) {
                throw new DuplicateKeyException("Duplicate request detected for key: " + key);
            }
            log.debug("Successfully set cache key: {}", key);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis is unavailable for cache set operation, skipping check", e);
        }
    }

    /**
     * Sets a value in Redis with a custom TTL. Overwrites the value if the key already exists.
     *
     * @param key the cache key
     * @param value the value to cache
     * @param ttlSeconds the time-to-live in seconds
     * @throws RedisConnectionFailureException caught and logged if Redis is unavailable
     */
    @Override
    public void setValueWithTTL(String key, String value, long ttlSeconds) {
        try {
            Duration duration = Duration.ofSeconds(ttlSeconds);
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, value, duration);

            if (Boolean.FALSE.equals(isNew)) {
                log.debug(
                        "Cache key {} already exists. Overwriting with new value and TTL of {}"
                                + " seconds.",
                        key,
                        ttlSeconds);
                redisTemplate.opsForValue().set(key, value, duration);
            } else {
                log.debug("Successfully set cache key: {} with TTL of {} seconds", key, ttlSeconds);
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis is unavailable for cache set operation, skipping", e);
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
