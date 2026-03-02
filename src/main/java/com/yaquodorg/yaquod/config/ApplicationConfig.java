package com.yaquodorg.yaquod.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yaquodorg.yaquod.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationConfig {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String webClientId;

    @Value("${google.android.client-id}")
    private String androidClientId;

    @Value("${google.ios.client-id}")
    private String iosClientId;

    private final UserService userService;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userService.getUser(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        try {
            log.info("Initializing FirebaseMessaging bean");

            FirebaseApp app;

            if (FirebaseApp.getApps().isEmpty()) {
                log.info("No FirebaseApp found, creating a new one");

                InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();

                GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount);

                FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                        .setCredentials(googleCredentials)
                        .build();

                app = FirebaseApp.initializeApp(firebaseOptions, "yaquod");

                log.info("Firebase application initialized successfully");
            } else {
                log.info("FirebaseApp already exists, reusing it");
                app = FirebaseApp.getInstance("yaquod");
            }

            return FirebaseMessaging.getInstance(app);

        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw e;
        }
    }


    /**
     * Builds the GoogleIdTokenVerifier with all possible client IDs
     * (Web, Android, iOS) to support tokens from different platforms.
     */
    @Bean
    public GoogleIdTokenVerifier buildVerifier() {
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
