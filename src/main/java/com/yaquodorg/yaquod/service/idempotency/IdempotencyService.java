package com.yaquodorg.yaquod.service.idempotency;

public interface IdempotencyService {

    void validate(String key, String paymentId);

    void invalidate(String key, String paymentId);

    String findExistingKey(String key, String paymentId);
}
