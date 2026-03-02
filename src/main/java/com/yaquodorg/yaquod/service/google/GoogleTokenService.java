package com.yaquodorg.yaquod.service.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.yaquodorg.yaquod.dtos.GoogleLoginDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@Slf4j
public class GoogleTokenService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String webClientId;

    @Value("${google.android.client-id}")
    private String androidClientId;

    @Value("${google.ios.client-id}")
    private String iosClientId;

    /**
     * Verifies the Google ID token received from Flutter Google Sign-In.
     *
     * @param idTokenString The ID token string received from the mobile app
     * @return GoogleLoginDto containing user information if token is valid
     * @throws GeneralSecurityException if there's a security error during verification
     * @throws IOException if there's an I/O error during verification
     * @throws IllegalArgumentException if the token is invalid or expired
     */
    public GoogleLoginDto verifyIdToken(String idTokenString) throws GeneralSecurityException, IOException {
        log.info("Verifying Google ID token");

        GoogleIdTokenVerifier verifier = buildVerifier();
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
        String pictureUrl = (String) payload.get("picture");

        log.info("Google ID token verified successfully for email: {}", email);
        log.debug("Token payload - name: {}, givenName: {}, familyName: {}, emailVerified: {}",
                name, givenName, familyName, emailVerified);

        if (!emailVerified) {
            log.warn("Email not verified for Google user: {}", email);
            throw new IllegalArgumentException("Email is not verified by Google");
        }

        return new GoogleLoginDto(email, name, givenName, familyName);
    }

    /**
     * Builds the GoogleIdTokenVerifier with all possible client IDs
     * (Web, Android, iOS) to support tokens from different platforms.
     */
    private GoogleIdTokenVerifier buildVerifier() {
        log.debug("Building Google ID token verifier");

        // Build list of valid client IDs (web, android, ios)
        java.util.List<String> clientIds = new java.util.ArrayList<>();
        clientIds.add(webClientId);

        if (androidClientId != null && !androidClientId.isEmpty()) {
            clientIds.add(androidClientId);
            log.debug("Android client ID configured");
        }

        if (iosClientId != null && !iosClientId.isEmpty()) {
            clientIds.add(iosClientId);
            log.debug("iOS client ID configured");
        }

        log.debug("Verifier configured with {} client ID(s)", clientIds.size());

        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(clientIds)
                .build();
    }
}
