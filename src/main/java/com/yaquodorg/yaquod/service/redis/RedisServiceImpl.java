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
    public static final String REQUEST_TIMEOUT_PREFIX = "request:timeout:";

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Sets a value in Redis with the default TTL (5 minutes). Throws an exception if the key
     * already exists to prevent duplicate requests.
     *
     * @param key the cache key
     * @param value the value to cache
     * @throws DuplicateKeyException if the key already exists
     * @throws RedisConnectionFailureException caught and logged if Redis is unavailable
     */
    @Override
    public void set(String key, String value) {
        try {
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, value, DEFAULT_TTL);
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
    public void setWithTtl(String key, String value, long ttlSeconds) {
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

    /**
     * Retrieves a value from Redis cache.
     *
     * @param key the cache key
     * @return the cached value, or null if the key doesn't exist or Redis is unavailable
     */
    @Override
    public String get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
            } else {
                log.debug("Cache miss for key: {}", key);
            }
            return value;
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis is unavailable for cache get operation, returning null", e);
            return null;
        }
    }

    /**
     * Removes a key from Redis cache.
     *
     * @param key the cache key to invalidate
     * @throws RedisConnectionFailureException caught and logged if Redis is unavailable
     */
    @Override
    public void invalidate(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Successfully invalidated cache key: {}", key);
            } else {
                log.debug("Cache key not found for invalidation: {}", key);
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis is unavailable for cache invalidation, skipping", e);
        }
    }
}
