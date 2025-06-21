package com.meta.accesscontrol.security.jwt;

import com.meta.accesscontrol.config.JwtProperties;
import com.meta.accesscontrol.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtils {
    private final JwtProperties jwtProperties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private String generateToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key())
                .compact();
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        List<String> authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        Map<String, Object> claims = new HashMap<>();
        claims.put("textId", userPrincipal.getTextId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("authorities", authorities);
        return generateToken(claims, userPrincipal.getUsername(), jwtProperties.getExpirationMs());
    }

    public String getUsernameClaim(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String generateRefreshToken(UserDetailsImpl userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("textId", userPrincipal.getTextId());
        claims.put("email", userPrincipal.getEmail());
        return generateToken(claims, userPrincipal.getUsername(), jwtProperties.getRefreshExpirationMs());
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Refresh token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Refresh token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Refresh token claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromRefreshToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String generateJwtToken(Authentication authentication, long expirationMs) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        List<String> authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        Map<String, Object> claims = new HashMap<>();
        claims.put("textId", userPrincipal.getTextId());
        claims.put("email", userPrincipal.getEmail());
        claims.put("authorities", authorities);
        return generateToken(claims, userPrincipal.getUsername(), expirationMs);
    }

    public String generateRefreshToken(UserDetailsImpl userPrincipal, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("textId", userPrincipal.getTextId());
        claims.put("email", userPrincipal.getEmail());
        return generateToken(claims, userPrincipal.getUsername(), expirationMs);
    }

    public JwtProperties getJwtProperties() {
        return jwtProperties;
    }
}