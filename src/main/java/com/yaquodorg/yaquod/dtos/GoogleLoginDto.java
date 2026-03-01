package com.yaquodorg.yaquod.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Google login user info.
 * Used when authenticating via Google OAuth.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String name;

    private String givenName;

    private String familyName;
}
