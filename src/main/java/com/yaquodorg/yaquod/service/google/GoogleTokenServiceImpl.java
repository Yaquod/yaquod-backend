package com.yaquodorg.yaquod.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.yaquodorg.yaquod.dtos.GoogleLoginDto;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleTokenServiceImpl implements GoogleTokenService {
  private final GoogleIdTokenVerifier verifier;

  public GoogleTokenServiceImpl(GoogleIdTokenVerifier verifier) {
    this.verifier = verifier;
  }

  /**
   * Verifies the Google ID token received from Flutter Google Sign-In.
   *
   * @param idTokenString The ID token string received from the mobile app
   * @return GoogleLoginDto containing user information if token is valid
   * @throws IllegalArgumentException if the token is invalid or expired
   */
  @Operation
  public GoogleLoginDto verifyIdToken(String idTokenString)
      throws GeneralSecurityException, IOException {
    log.info("Verifying Google ID token");
    GoogleIdToken idToken = verifier.verify(idTokenString);

    if (idToken == null) {
      log.error("Invalid Google ID token");
      throw new IllegalArgumentException("Invalid Google ID token");
    }

    GoogleIdToken.Payload payload = idToken.getPayload();

    String email = payload.getEmail();
    boolean emailVerified = payload.getEmailVerified();
    String name = (String) payload.get("name");
    String givenName = (String) payload.get("given_name");
    String familyName = (String) payload.get("family_name");

    log.info("Google ID token verified successfully for email: {}", email);
    log.debug(
        "Token payload - name: {}, givenName: {}, familyName: {}, emailVerified: {}",
        name,
        givenName,
        familyName,
        emailVerified);

    if (!emailVerified) {
      log.warn("Email not verified for Google user: {}", email);
      throw new IllegalArgumentException("Email is not verified by Google");
    }

    return new GoogleLoginDto(email, name, givenName, familyName);
  }

  /**
   * Builds the GoogleIdTokenVerifier with all possible client IDs (Web, Android, iOS) to support
   * tokens from different platforms.
   */
}
