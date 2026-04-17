package com.tbank.marketplace.security;


import com.tbank.marketplace.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:kj16bC8tHJyc9UnDCi8uDxynBKofS9sQpb3lRX6A2TQ}")
    private String secret;

    @Value("${jwt.access.expiration:900000}")
    private Long accessExpiration;

    @Value("${jwt.refresh.expiration:604800000}")
    private Long refreshExpiration;

    private SecretKey getSigningKey(){
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String generateAccessToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId().toString());

        return createToken(claims, user.getUsername(), accessExpiration);
    }

    public String generateRefreshToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId().toString());
        return createToken(claims, user.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        return Jwts
                .builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && isRefreshToken(token));
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token) && isAccessToken(token));
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUserId(String token){return extractClaim(token, claims -> claims.get("userId", String.class));}

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean isRefreshToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isAccessToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "access".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
