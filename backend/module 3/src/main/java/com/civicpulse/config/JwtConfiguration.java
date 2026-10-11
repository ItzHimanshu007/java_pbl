package com.civicpulse.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
@EnableConfigurationProperties(JwtSettings.class)
public class JwtConfiguration {
  @Bean
  SecretKey jwtSigningKey(JwtSettings settings) {
    byte[] bytes;
    if (settings.secret() == null || settings.secret().isBlank()) {
      if (!settings.allowEphemeralKey()) {
        throw new IllegalStateException(
            "Set CP_JWT_SECRET to a Base64-encoded random key of at least 32 bytes");
      }
      bytes = new byte[32];
      new SecureRandom().nextBytes(bytes);
    } else {
      try {
        bytes = Base64.getDecoder().decode(settings.secret());
      } catch (IllegalArgumentException ex) {
        throw new IllegalStateException("CP_JWT_SECRET must be valid Base64");
      }
    }
    if (bytes.length < 32)
      throw new IllegalStateException("JWT signing key must contain at least 32 random bytes");
    if (settings.tokenLifetime() == null
        || settings.tokenLifetime().isNegative()
        || settings.tokenLifetime().isZero()
        || settings.tokenLifetime().compareTo(java.time.Duration.ofHours(8)) > 0) {
      throw new IllegalStateException("JWT lifetime must be between one second and eight hours");
    }
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey key, JwtSettings settings) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(settings.issuer()));
    return decoder;
  }
}
