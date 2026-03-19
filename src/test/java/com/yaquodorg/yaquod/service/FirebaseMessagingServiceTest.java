package com.yaquodorg.yaquod.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.yaquodorg.yaquod.service.messaging.FirebaseMessagingServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** NOTE: ALL THOSE TESTS ARE AI-GENERATED AND REVIEWED MANUALLY */
@ExtendWith(MockitoExtension.class)
@DisplayName("FirebaseMessagingService Unit Tests")
class FirebaseMessagingServiceTest {

    @Mock private FirebaseMessaging firebaseMessaging;

    @InjectMocks private FirebaseMessagingServiceImpl firebaseMessagingService;

    @Test
    @DisplayName("Should send notification successfully")
    void shouldSendNotificationSuccessfully() throws FirebaseMessagingException {
        // Arrange
        when(firebaseMessaging.send(any(Message.class))).thenReturn("message-id-123");

        // Act
        firebaseMessagingService.sendTextNotificationByToken(
                "valid-fcm-token", "Test Title", "Test Body");

        // Assert
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    @DisplayName("Should not send notification when token is null")
    void shouldNotSendWhenTokenIsNull() throws FirebaseMessagingException {
        // Act
        firebaseMessagingService.sendTextNotificationByToken(null, "Title", "Body");

        // Assert
        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("Should not send notification when token is empty")
    void shouldNotSendWhenTokenIsEmpty() throws FirebaseMessagingException {
        // Act
        firebaseMessagingService.sendTextNotificationByToken("", "Title", "Body");

        // Assert
        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("Should handle FirebaseMessagingException gracefully")
    void shouldHandleFirebaseMessagingException() throws FirebaseMessagingException {
        // Arrange
        when(firebaseMessaging.send(any(Message.class)))
                .thenThrow(FirebaseMessagingException.class);

        // Act - Should not throw
        firebaseMessagingService.sendTextNotificationByToken("valid-token", "Title", "Body");

        // Assert
        verify(firebaseMessaging).send(any(Message.class));
    }

    @Test
    @DisplayName("Should handle unexpected exceptions gracefully")
    void shouldHandleUnexpectedException() throws FirebaseMessagingException {
        // Arrange
        when(firebaseMessaging.send(any(Message.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act - Should not throw
        firebaseMessagingService.sendTextNotificationByToken("valid-token", "Title", "Body");

        // Assert
        verify(firebaseMessaging).send(any(Message.class));
    }
}
