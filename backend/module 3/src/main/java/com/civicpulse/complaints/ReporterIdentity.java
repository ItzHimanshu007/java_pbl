package com.civicpulse.complaints;

import com.civicpulse.account.UserDao;
import com.civicpulse.shared.Role;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * Pseudonymous reporter identity. Every report stores HMAC-SHA256(key, account id) so the
 * reporter can prove ownership, while an anonymous report stores no name, email or account id.
 * Without the server key the hash cannot be linked back to an account. The key comes from
 * CP_ANONYMITY_SECRET, or is derived from the JWT signing key when that is not configured.
 */
@Component
public class ReporterIdentity {
  public static final String ANONYMOUS_EMAIL = "anonymous@civicpulse.invalid";
  public static final String ANONYMOUS_NAME = "Anonymous citizen";
  private final SecretKeySpec key;
  private final UserDao users;

  public ReporterIdentity(
      @Value("${civic.anonymity.secret:}") String secret, SecretKey jwtKey, UserDao users) {
    this.users = users;
    byte[] material;
    if (secret == null || secret.isBlank()) {
      material = hmac(new SecretKeySpec(jwtKey.getEncoded(), "HmacSHA256"), "civicpulse-anonymity-v1");
    } else {
      try {
        material = Base64.getDecoder().decode(secret);
      } catch (IllegalArgumentException ex) {
        throw new IllegalStateException("CP_ANONYMITY_SECRET must be valid Base64");
      }
      if (material.length < 32)
        throw new IllegalStateException("CP_ANONYMITY_SECRET must contain at least 32 random bytes");
    }
    key = new SecretKeySpec(material, "HmacSHA256");
  }

  /** Stable keyed hash of an account; 64 lowercase hexadecimal characters. */
  public String hash(long accountId) {
    return HexFormat.of().formatHex(hmac(key, "reporter:" + accountId));
  }

  public boolean matches(String storedHash, long accountId) {
    return storedHash != null
        && MessageDigest.isEqual(
            storedHash.getBytes(StandardCharsets.US_ASCII),
            hash(accountId).getBytes(StandardCharsets.US_ASCII));
  }

  /** Disabled system account that anonymous reports and their owner's actions are filed under. */
  public long anonymousAccount() {
    var existing = users.findByEmail(ANONYMOUS_EMAIL);
    if (existing.isPresent()) return existing.get().id();
    try {
      var created = users.create(ANONYMOUS_NAME, ANONYMOUS_EMAIL, "!disabled-system-account", Role.CITIZEN);
      users.disable(created.id());
      return created.id();
    } catch (DuplicateKeyException ex) {
      return users.findByEmail(ANONYMOUS_EMAIL).orElseThrow().id();
    }
  }

  private static byte[] hmac(SecretKeySpec key, String message) {
    try {
      var mac = Mac.getInstance("HmacSHA256");
      mac.init(key);
      return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("HMAC-SHA256 is unavailable", ex);
    }
  }
}
