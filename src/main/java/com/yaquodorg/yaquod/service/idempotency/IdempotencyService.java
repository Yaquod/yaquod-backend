package com.yaquodorg.yaquod.service.idempotency;

public interface IdempotencyService {

    public void validate(String key, String userId);
}
