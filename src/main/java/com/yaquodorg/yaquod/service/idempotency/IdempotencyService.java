package com.yaquodorg.yaquod.service.idempotency;

public interface IdempotencyService {

    void validate(String key);

    void invalidate(String key);

    String findExistingKey(String key);
}
