package com.yaquodorg.yaquod.service.redis;

public interface RedisService {

    String getValue(String key);

    void setValue(String key, String value, long ttlSeconds);

    void delete(String key);
}
