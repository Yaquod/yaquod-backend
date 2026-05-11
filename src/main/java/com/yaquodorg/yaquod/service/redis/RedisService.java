package com.yaquodorg.yaquod.service.redis;

public interface RedisService {

    void setValue(String key, String value);

    void setValueWithTTL(String key, String value, long ttlSeconds);

    String getValue(String key);

    void delete(String key);
}
