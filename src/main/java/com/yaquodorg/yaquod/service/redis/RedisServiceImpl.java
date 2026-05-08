package com.yaquodorg.yaquod.service.redis;

import com.yaquodorg.yaquod.exception.DuplicateKeyException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Boolean isNew =
                redisTemplate.opsForValue().setIfAbsent("idempotency:" + key, "pending", TTL);

        if (Boolean.FALSE.equals(isNew)) {
            throw new DuplicateKeyException("Duplicate request detected for key: " + key);
        }
    }

    @Override
    public void invalidate(String key) {
        redisTemplate.delete("idempotency:" + key);
    }

    @Override
    public String findExistingKey(String key) {
        String redisKey = "idempotency:" + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.info("Existing idempotency key found: {}", redisKey);
        }
        return value;
    }

    @Override
    public void setValue(String key, String value, long ttlSeconds) {
        Boolean isNew =
                redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(ttlSeconds));
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Key {} already exists in Redis. Overwriting with new value.", key);
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        }
    }
}
