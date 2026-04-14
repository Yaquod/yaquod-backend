package com.yaquodorg.yaquod.service.idempotency;

import com.yaquodorg.yaquod.exception.DuplicateKeyException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyServiceImpl implements IdempotencyService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(5);

    @Override
    public void validate(String key, String paymentId) {
        String redisKey = "idempotency:" + paymentId + ":" + key;

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "pending", TTL);

        if (Boolean.FALSE.equals(isNew)) {
            throw new DuplicateKeyException("Duplicate request detected for key: " + key);
        }
    }

    @Override
    public void invalidate(String key, String paymentId) {
        String redisKey = "idempotency:" + paymentId + ":" + key;
        redisTemplate.delete(redisKey);
    }

    @Override
    public String findExistingKey(String key, String paymentId) {
        String redisKey = "idempotency:" + paymentId + ":" + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.info("Existing idempotency key found: {}", redisKey);
        }
        return value;
    }
}
