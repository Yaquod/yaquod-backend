package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Google OAuth login request from mobile apps (Flutter). Contains the
 * Google ID token received from Google Sign-In.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleIdTokenDto {

    @NotBlank(message = "ID token is required")
    private String idToken;

    /** Optional FCM token for push notifications. */
    private String fcmToken;
}
