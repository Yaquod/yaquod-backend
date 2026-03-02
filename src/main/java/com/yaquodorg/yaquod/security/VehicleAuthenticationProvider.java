package com.yaquodorg.yaquod.security;

import com.yaquodorg.yaquod.entity.Vehicle;
import com.yaquodorg.yaquod.repository.VehicleRepository;
import java.sql.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleAuthenticationProvider implements AuthenticationProvider {

    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        VehicleAuthenticationToken token = (VehicleAuthenticationToken) authentication;

        String apiKey = token.getApiKey();
        String apiSecret = (String) token.getCredentials();

        // Find vehicle by API key
        Vehicle vehicle = vehicleRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BadCredentialsException("Invalid vehicle credentials"));

        // Verify the API secret
        if (!passwordEncoder.matches(apiSecret, vehicle.getApiSecretHash())) {
            throw new BadCredentialsException("Invalid vehicle credentials");
        }

        // Update last authenticated timestamp
        vehicle.setLastAuthenticatedAt(new Timestamp(System.currentTimeMillis()));
        vehicleRepository.save(vehicle);

        // Create authenticated token with vehicle details
        VehicleAuthenticationToken authenticatedToken = new VehicleAuthenticationToken(vehicle.getId(),
                vehicle.getApiKey(), vehicle.getCreatedByAdmin().getId(),
                // No claims needed here, will be in JWT
                null);

        // Store vehicle in details for easy retrieval
        authenticatedToken.setDetails(vehicle);

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return VehicleAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
