package com.civicpulse.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "civic.security")
public record JwtSettings(
    String secret,
    String issuer,
    Duration tokenLifetime,
    boolean secureCookie,
    boolean allowEphemeralKey) {}
