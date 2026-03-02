package com.yaquodorg.yaquod.service.messaging;

public interface FirebaseMessagingService {

    void sendTextNotificationByToken(String token, String title, String body);
}
