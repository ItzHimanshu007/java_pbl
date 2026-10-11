package com.civicpulse.account;

import com.civicpulse.shared.Role;
import java.sql.PreparedStatement;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {
  private final JdbcTemplate jdbc;
  private final RowMapper<AccountUser> mapper =
      (rs, row) ->
          new AccountUser(
              rs.getLong("id"),
              rs.getString("display_name"),
              rs.getString("email"),
              rs.getString("password_hash"),
              Role.valueOf(rs.getString("role")),
              rs.getBoolean("enabled"),
              rs.getTimestamp("created_at").toInstant());

  public UserDao(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<AccountUser> findByEmail(String email) {
    return jdbc.query("SELECT * FROM users WHERE email = ?", mapper, email).stream().findFirst();
  }

  public Optional<AccountUser> findById(long id) {
    return jdbc.query("SELECT * FROM users WHERE id = ?", mapper, id).stream().findFirst();
  }

  public AccountUser create(String name, String email, String hash, Role role) {
    var key = new GeneratedKeyHolder();
    jdbc.update(
        connection -> {
          PreparedStatement statement =
              connection.prepareStatement(
                  "INSERT INTO users(display_name,email,password_hash,role) VALUES (?,?,?,?)",
                  new String[] {"id"});
          statement.setString(1, name);
          statement.setString(2, email);
          statement.setString(3, hash);
          statement.setString(4, role.name());
          return statement;
        },
        key);
    Number id = key.getKey();
    if (id == null) throw new IllegalStateException("User ID was not returned");
    return findById(id.longValue()).orElseThrow();
  }

  public void updateDisplayName(long id, String name) {
    jdbc.update("UPDATE users SET display_name = ? WHERE id = ?", name, id);
  }
}
