package com.civicpulse.auth;

import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoginSessionDao {
  private final JdbcTemplate jdbc;

  public LoginSessionDao(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void create(String tokenId, long userId, Instant expiry) {
    jdbc.update(
        "INSERT INTO login_sessions(token_id,user_id,expires_at) VALUES (?,?,?)",
        tokenId,
        userId,
        Timestamp.from(expiry));
  }

  public boolean isActive(String tokenId, long userId, Instant now) {
    Integer count =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM login_sessions WHERE token_id = ? AND user_id = ? AND revoked ="
                + " FALSE AND expires_at > ?",
            Integer.class,
            tokenId,
            userId,
            Timestamp.from(now));
    return count != null && count == 1;
  }

  public void revoke(String tokenId) {
    jdbc.update("UPDATE login_sessions SET revoked = TRUE WHERE token_id = ?", tokenId);
  }
}
