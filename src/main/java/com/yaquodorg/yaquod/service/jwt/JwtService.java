package com.yaquodorg.yaquod.service.jwt;

import org.springframework.security.core.userdetails.UserDetails;

import com.yaquodorg.yaquod.entity.Vehicle;

import io.jsonwebtoken.Claims;

import java.util.Date;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    String generateVehicleToken(Vehicle vehicle);

    String generateVehicleRefreshToken(Vehicle vehicle);

    String getTokenType(String token);

    String getEmailFromToken(String token);

    boolean validateToken(String token);

    Date extractExpiration(String token);

    Claims extractAllClaims(String token);
}
