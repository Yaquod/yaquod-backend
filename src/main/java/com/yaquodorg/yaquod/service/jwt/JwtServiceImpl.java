package com.yaquodorg.yaquod.service.jwt;

import com.yaquodorg.yaquod.entity.Role;
import com.yaquodorg.yaquod.entity.Vehicle;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${spring.application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${spring.application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${spring.application.security.jwt.vehicle-token-expiration}")
    private long vehicleTokenExpiration;

    @Value("${spring.application.security.jwt.vehicle-refresh-token-expiration}")
    private long vehicleRefreshTokenExpiration;

    private SecretKey signingKey;

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("roles", userDetails.getAuthorities())
                .setId(UUID.randomUUID().toString())
                .signWith(getKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("roles", userDetails.getAuthorities())
                .setId(UUID.randomUUID().toString())
                .signWith(getKey())
                .compact();
    }

    @Override
    public String generateVehicleToken(Vehicle vehicle) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + vehicleTokenExpiration);

        return Jwts.builder()
                .setSubject(vehicle.getApiKey())
                .setIssuedAt(now)
                .claim("roles", Role.VEHICLE)
                .claim("vehicleId", vehicle.getId())
                .claim("adminId", vehicle.getCreatedByAdmin().getId())
                .setId(UUID.randomUUID().toString())
                .setExpiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    @Override
    public String generateVehicleRefreshToken(Vehicle vehicle) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + vehicleRefreshTokenExpiration);

        return Jwts.builder()
                .setSubject(vehicle.getApiKey())
                .setIssuedAt(now)
                .claim("roles", Role.VEHICLE)
                .claim("vehicleId", vehicle.getId())
                .claim("adminId", vehicle.getCreatedByAdmin().getId())
                .setId(UUID.randomUUID().toString())
                .setExpiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    @Override
    public String getTokenType(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get("roles");

        if (roles != null && roles.toString().contains(Role.VEHICLE.toString())) {
            return "vehicle";
        }
        return "user";
    }

    @Override
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private SecretKey getKey() {
        if (signingKey == null) {
            signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        }
        return signingKey;
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
    }
}
