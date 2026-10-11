package com.civicpulse.account;

import java.sql.Date;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProfileDao {
  private final JdbcTemplate jdbc;

  public ProfileDao(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public AccountProfile find(long id) {
    return jdbc
        .query(
            "SELECT * FROM account_profiles WHERE user_id=?",
            (r, n) ->
                new AccountProfile(
                    r.getDate("date_of_birth") == null
                        ? null
                        : r.getDate("date_of_birth").toLocalDate(),
                    r.getString("phone"),
                    r.getString("address"),
                    r.getString("city"),
                    r.getString("postal_code"),
                    r.getString("neighbourhood"),
                    r.getString("language"),
                    r.getString("theme"),
                    (Double) r.getObject("home_latitude"),
                    (Double) r.getObject("home_longitude")),
            id)
        .stream()
        .findFirst()
        .orElse(AccountProfile.empty());
  }

  @Transactional
  public void save(long id, ProfileForm f) {
    // Lock the existing account so concurrent first saves cannot create duplicate profile rows.
    jdbc.queryForList("SELECT id FROM users WHERE id=? FOR UPDATE", id);
    if (jdbc.queryForObject("SELECT COUNT(*) FROM account_profiles WHERE user_id=?", Long.class, id)
        == 0) jdbc.update("INSERT INTO account_profiles(user_id) VALUES(?)", id);
    jdbc.update(
        "UPDATE account_profiles SET"
            + " date_of_birth=?,phone=?,address=?,city=?,postal_code=?,neighbourhood=?,language=?,theme=?,home_latitude=?,home_longitude=?"
            + " WHERE user_id=?",
        f.getDateOfBirth() == null ? null : Date.valueOf(f.getDateOfBirth()),
        clean(f.getPhone()),
        clean(f.getAddress()),
        clean(f.getCity()),
        clean(f.getPostalCode()),
        clean(f.getNeighbourhood()),
        f.getLanguage(),
        f.getTheme(),
        f.getHomeLatitude(),
        f.getHomeLongitude(),
        id);
    jdbc.update("UPDATE users SET display_name=? WHERE id=?", f.getDisplayName().strip(), id);
  }

  /** Saves only the given display preferences; {@code null} leaves a preference unchanged. */
  @Transactional
  public void savePreferences(long id, String language, String theme) {
    jdbc.queryForList("SELECT id FROM users WHERE id=? FOR UPDATE", id);
    if (jdbc.queryForObject("SELECT COUNT(*) FROM account_profiles WHERE user_id=?", Long.class, id)
        == 0) jdbc.update("INSERT INTO account_profiles(user_id) VALUES(?)", id);
    if (language != null)
      jdbc.update("UPDATE account_profiles SET language=? WHERE user_id=?", language, id);
    if (theme != null) jdbc.update("UPDATE account_profiles SET theme=? WHERE user_id=?", theme, id);
  }

  private String clean(String v) {
    return v == null ? "" : v.strip();
  }
}
