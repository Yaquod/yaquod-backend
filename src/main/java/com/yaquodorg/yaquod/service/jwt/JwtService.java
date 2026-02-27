package com.yaquodorg.yaquod.service.jwt;

import org.springframework.security.core.userdetails.UserDetails;

import com.yaquodorg.yaquod.entity.Vehicle;

import java.util.Date;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);

    String generateRefreshToken(UserDetails userDetails);

    String generateVehicleToken(Vehicle vehicle);

    String getEmailFromToken(String token);

    boolean validateToken(String token);

    Date extractExpiration(String token);
}
