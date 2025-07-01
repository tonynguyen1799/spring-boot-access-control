package com.meta.accesscontrol.security.jwt;

import com.meta.accesscontrol.config.JwtProperties;
import com.meta.accesscontrol.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
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

@Getter
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

    // --- This private helper method contains the consolidated validation logic ---
    private boolean validateToken(String token, String tokenType) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid {}: {}", tokenType, e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("{} is expired: {}", tokenType, e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("{} is unsupported: {}", tokenType, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("{} claims string is empty: {}", tokenType, e.getMessage());
        }
        return false;
    }

    public boolean validateJwtToken(String authToken) {
        return validateToken(authToken, "JWT token");
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, "Refresh token");
    }

    public String getUsernameClaim(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String getUsernameFromRefreshToken(String token) {
        return Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    // --- Other methods for token generation remain the same ---

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

}