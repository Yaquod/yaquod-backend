package com.yaquodorg.yaquod.service.messaging;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseMessagingServiceImpl implements FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendTextNotificationByToken(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            log.warn("Cannot send notification: token is null or empty");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("Successfully sent notification: {} and token: {}", response, token);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while sending notification", e);
        }
    }
}
