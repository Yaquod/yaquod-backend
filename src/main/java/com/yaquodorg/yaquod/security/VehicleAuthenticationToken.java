package com.yaquodorg.yaquod.security;

import io.jsonwebtoken.Claims;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class VehicleAuthenticationToken implements Authentication {

  private final Long vehicleId;
  private final String apiKey;
  private final String apiSecret;
  private final Long adminId;
  private final Claims claims;
  private boolean authenticated;
  private Object details;

  /**
   * Constructor for authentication ( before login ) Used when vehicle is trying to authenticate
   * with apiKey + apiSecret
   */
  public VehicleAuthenticationToken(String apiKey, String apiSecret) {
    this.vehicleId = null;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
    this.adminId = null;
    this.claims = null;
    this.authenticated = false;
  }

  /**
   * Constructor for authenticated token ( after successful login ) Used after vehicle has been
   * authenticated
   */
  public VehicleAuthenticationToken(Long vehicleId, String apiKey, Long adminId, Claims claims) {
    this.vehicleId = vehicleId;
    this.apiKey = apiKey;
    this.apiSecret = null;
    this.adminId = adminId;
    this.claims = claims;
    this.authenticated = true;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Vehicle has limited permissions
    return List.of(new SimpleGrantedAuthority("ROLE_VEHICLE"));
  }

  @Override
  public Object getCredentials() {
    // Return apiSecret during authentication, null after
    return apiSecret;
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
    // Return vehicleId if authenticated, apiKey if authenticating
    return vehicleId != null ? vehicleId : apiKey;
  }

  @Override
  public boolean isAuthenticated() {
    return authenticated;
  }

  @Override
  public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
    this.authenticated = authenticated;
  }

  @Override
  public String getName() {
    return apiKey;
  }

  public Long getVehicleId() {
    return vehicleId;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public Long getAdminId() {
    return adminId;
  }

  public Claims getClaims() {
    return claims;
  }
}
