package com.civicpulse.auth;

import com.civicpulse.account.AccountUser;
import com.civicpulse.config.JwtSettings;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
  private final JwtEncoder encoder;
  private final LoginSessionDao sessions;
  private final JwtSettings settings;

  public JwtTokenService(JwtEncoder encoder, LoginSessionDao sessions, JwtSettings settings) {
    this.encoder = encoder;
    this.sessions = sessions;
    this.settings = settings;
  }

  public String issue(AccountUser user) {
    Instant now = Instant.now();
    Instant expiry = now.plus(settings.tokenLifetime());
    String id = UUID.randomUUID().toString();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(settings.issuer())
            .subject(Long.toString(user.id()))
            .id(id)
            .issuedAt(now)
            .expiresAt(expiry)
            .claim("role", user.role().name())
            .build();
    String token =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    sessions.create(id, user.id(), expiry);
    return token;
  }
}
