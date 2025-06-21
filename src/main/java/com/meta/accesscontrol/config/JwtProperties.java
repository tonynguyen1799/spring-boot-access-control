package com.meta.accesscontrol.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    // Maps to app.jwt.expiration-ms
    private long expirationMs;
    // Maps to app.jwt.refresh-expiration-ms
    private long refreshExpirationMs;
    // Maps to app.jwt.long-expiration-ms
    private long longExpirationMs;
    // Maps to app.jwt.long-refresh-expiration-ms
    private long longRefreshExpirationMs;
}