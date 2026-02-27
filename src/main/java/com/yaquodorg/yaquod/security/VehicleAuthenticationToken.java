package com.yaquodorg.yaquod.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class VehicleAuthenticationToken implements Authentication {

    private final Long vehicleId;
    private final String apiKey;
    private final Long adminId;
    private final Claims claims;
    private boolean authenticated = true;
    private Object details;

    public VehicleAuthenticationToken(Long vehicleId, String apiKey, Long adminId, Claims claims) {
        this.vehicleId = vehicleId;
        this.apiKey = apiKey;
        this.adminId = adminId;
        this.claims = claims;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Vehicle has limited permissions
        return List.of(new SimpleGrantedAuthority("ROLE_VEHICLE"));
    }

    @Override
    public Object getCredentials() {
        // Token already validated
        return null;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }

    @Override
    public Object getPrincipal() {
        return vehicleId;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return apiKey;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public Claims getClaims() {
        return claims;
    }
}
