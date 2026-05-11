package com.yaquodorg.yaquod.service.redis;

/**
 * Service for managing Redis cache operations. Provides methods for setting, getting, and
 * invalidating cached values.
 */
public interface RedisService {

    /**
     * Sets a value in Redis with the default TTL (5 minutes).
     *
     * @param key the cache key
     * @param value the value to cache
     * @throws DuplicateKeyException if the key already exists
     */
    void set(String key, String value);

    /**
     * Sets a value in Redis with a custom TTL.
     *
     * @param key the cache key
     * @param value the value to cache
     * @param ttlSeconds the time-to-live in seconds
     */
    void setWithTtl(String key, String value, long ttlSeconds);

    /**
     * Retrieves a value from Redis cache.
     *
     * @param key the cache key
     * @return the cached value, or null if not found
     */
    String get(String key);

    /**
     * Removes a key from Redis cache.
     *
     * @param key the cache key to invalidate
     */
    void invalidate(String key);
}
