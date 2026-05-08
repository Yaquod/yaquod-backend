package com.yaquodorg.yaquod.service.redis;

public interface RedisService {

    void validate(String key);

    void invalidate(String key);

    String findExistingKey(String key);

    void setValue(String key, String value, long ttlSeconds);
}
