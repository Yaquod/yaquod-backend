package com.yaquodorg.yaquod.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleIdTokenDto {

    @NotBlank(message = "ID token is required")
    private String idToken;

    private String fcmToken;
}
