package com.civicpulse;

import java.sql.*;
import java.util.*;
import org.springframework.jdbc.core.*;

/** Deletes only fixture data in the configured disposable test database. */
public final class TestDataCleaner {
  private TestDataCleaner() {}

  public static void clear(JdbcTemplate jdbc) {
    Set<String> tables =
        jdbc.execute(
            (ConnectionCallback<Set<String>>)
                c -> {
                  var names = new HashSet<String>();
                  try (var r =
                      c.getMetaData()
                          .getTables(
                              c.getCatalog(), null, "%", new String[] {"TABLE", "BASE TABLE"})) {
                    while (r.next()) names.add(r.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
                  }
                  return names;
                });
    if (tables.contains("complaints")) jdbc.update("UPDATE complaints SET issue_root_id=NULL");
    for (String table :
        List.of(
            "complaint_supports",
            "administration_events",
            "attachments",
            "complaint_events",
            "complaints",
            "login_sessions",
            "account_profiles",
            "users")) if (tables.contains(table)) jdbc.update("DELETE FROM " + table);
  }
}
