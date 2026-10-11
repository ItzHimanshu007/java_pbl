package com.civicpulse.complaints;

import com.civicpulse.shared.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ComplaintDao {
  private final JdbcTemplate jdbc;
  private final ReporterIdentity identity;
  private static final String SELECT =
      "SELECT c.*,k.name category,k.code category_code,k.sla_hours,d.name department,"
          + "u.display_name officer_name FROM complaints c"
          + " JOIN categories k ON k.id=c.category_id JOIN departments d ON d.id=c.department_id"
          + " LEFT JOIN users u ON u.id=c.assigned_officer_id ";

  private static Instant time(ResultSet r, String column) throws SQLException {
    var t = r.getTimestamp(column);
    return t == null ? null : t.toInstant();
  }

  private final RowMapper<Complaint> mapper =
      (r, n) ->
          new Complaint(
              r.getLong("id"),
              r.getString("reference"),
              r.getLong("reporter_id"),
              r.getLong("category_id"),
              r.getLong("department_id"),
              r.getString("category"),
              r.getString("department"),
              r.getString("title"),
              r.getString("description"),
              r.getString("address"),
              r.getDouble("latitude"),
              r.getDouble("longitude"),
              ComplaintStatus.valueOf(r.getString("status")),
              r.getObject("assigned_officer_id") == null ? null : r.getLong("assigned_officer_id"),
              r.getString("officer_name"),
              time(r, "created_at"),
              time(r, "updated_at"),
              time(r, "resolved_at"),
              time(r, "closed_at"),
              r.getString("category_code"),
              r.getInt("sla_hours"),
              r.getBoolean("is_anonymous"),
              r.getString("reporter_hash"));

  public ComplaintDao(JdbcTemplate jdbc, ReporterIdentity identity) {
    this.jdbc = jdbc;
    this.identity = identity;
  }

  public List<Category> categories() {
    return jdbc.query(
        "SELECT * FROM categories ORDER BY id",
        (r, n) ->
            new Category(
                r.getLong("id"),
                r.getString("name"),
                r.getLong("department_id"),
                r.getString("code"),
                r.getInt("sla_hours")));
  }

  public Category category(long id) {
    return categories().stream()
        .filter(c -> c.id() == id)
        .findFirst()
        .orElseThrow(() -> FeatureException.invalid("Choose a valid category."));
  }

  public Complaint find(long id) {
    return jdbc.query(SELECT + "WHERE c.id=? AND c.deleted_at IS NULL", mapper, id).stream()
        .findFirst()
        .orElseThrow(FeatureException::missing);
  }

  /** Call inside a transaction before checking or changing lifecycle state. */
  public Complaint lock(long id) {
    if (jdbc.queryForList("SELECT id FROM complaints WHERE id=? FOR UPDATE", id).isEmpty())
      throw FeatureException.missing();
    return find(id);
  }

  public Complaint findByReference(String reference) {
    return jdbc
        .query(
            SELECT + "WHERE c.reference=? AND c.deleted_at IS NULL",
            mapper,
            reference.strip().toUpperCase(Locale.ROOT))
        .stream()
        .findFirst()
        .orElseThrow(FeatureException::missing);
  }

  public long create(ComplaintForm f, long reporter, String reference) {
    return create(f, reporter, reference, false, identity.hash(reporter));
  }

  /**
   * Inserts a report. For an anonymous report {@code reporter} is the anonymous system account
   * and {@code reporterHash} is the only link to the real citizen.
   */
  public long create(
      ComplaintForm f, long reporter, String reference, boolean anonymous, String reporterHash) {
    var key = new GeneratedKeyHolder();
    var category = category(f.getCategoryId());
    jdbc.update(
        c -> {
          var s =
              c.prepareStatement(
                  "INSERT INTO"
                      + " complaints(reference,reporter_id,category_id,department_id,title,description,address,latitude,longitude,is_anonymous,reporter_hash)"
                      + " VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                  new String[] {"id"});
          s.setString(1, reference);
          s.setLong(2, reporter);
          s.setLong(3, category.id());
          s.setLong(4, category.departmentId());
          s.setString(5, f.getTitle().strip());
          s.setString(6, f.getDescription().strip());
          s.setString(7, f.getAddress().strip());
          s.setDouble(8, f.getLatitude());
          s.setDouble(9, f.getLongitude());
          s.setBoolean(10, anonymous);
          s.setString(11, reporterHash);
          return s;
        },
        key);
    return Objects.requireNonNull(key.getKey()).longValue();
  }

  public void softDelete(long id) {
    jdbc.update(
        "UPDATE complaints SET deleted_at=?,updated_at=? WHERE id=?",
        Timestamp.from(Instant.now()),
        Timestamp.from(Instant.now()),
        id);
  }

  public void restore(long id) {
    jdbc.update(
        "UPDATE complaints SET deleted_at=NULL,updated_at=? WHERE id=?",
        Timestamp.from(Instant.now()),
        id);
  }

  public Complaint lockDeleted(long id) {
    jdbc.queryForList("SELECT id FROM complaints WHERE id=? FOR UPDATE", id);
    return jdbc.query(SELECT + "WHERE c.id=? AND c.deleted_at IS NOT NULL", mapper, id).stream()
        .findFirst()
        .orElseThrow(FeatureException::missing);
  }

  public long issueRoot(long id) {
    return jdbc.queryForObject(
        "SELECT COALESCE(issue_root_id,id) FROM complaints WHERE id=?", Long.class, id);
  }

  public void link(long id, long root) {
    jdbc.update("UPDATE complaints SET issue_root_id=? WHERE id=?", root, id);
  }

  public void transition(long id, ComplaintStatus state, Long officer) {
    Instant now = Instant.now();
    if (state == ComplaintStatus.CLOSED) {
      jdbc.update(
          "UPDATE complaints SET status=?,assigned_officer_id=?,updated_at=?,closed_at=? WHERE"
              + " id=?",
          state.name(),
          officer,
          Timestamp.from(now),
          Timestamp.from(now),
          id);
      return;
    }
    jdbc.update(
        "UPDATE complaints SET"
            + " status=?,assigned_officer_id=?,updated_at=?,resolved_at=?,closed_at=? WHERE id=?",
        state.name(),
        officer,
        Timestamp.from(now),
        state == ComplaintStatus.RESOLVED ? Timestamp.from(now) : null,
        state == ComplaintStatus.CLOSED ? Timestamp.from(now) : null,
        id);
  }

  public void event(
      long id, long actor, String action, ComplaintStatus from, ComplaintStatus to, String note) {
    jdbc.update(
        "INSERT INTO"
            + " complaint_events(complaint_id,actor_id,action,from_status,to_status,note,created_at)"
            + " VALUES(?,?,?,?,?,?,?)",
        id,
        actor,
        action,
        from == null ? null : from.name(),
        to.name(),
        note,
        Timestamp.from(Instant.now()));
  }

  public List<ComplaintEvent> events(long id) {
    return jdbc.query(
        "SELECT e.*,u.display_name actor FROM complaint_events e JOIN users u ON u.id=e.actor_id"
            + " WHERE e.complaint_id=? ORDER BY e.id",
        (r, n) ->
            new ComplaintEvent(
                r.getLong("id"),
                r.getString("actor"),
                r.getString("action"),
                r.getString("from_status"),
                r.getString("to_status"),
                r.getString("note"),
                time(r, "created_at")),
        id);
  }

  public List<Complaint> list(
      String scope, Long user, String status, String query, int page, int size) {
    return list(scope, user, status, query, null, page, size);
  }

  public List<Complaint> list(
      String scope, Long user, String status, String query, Long category, int page, int size) {
    var args = new ArrayList<Object>();
    String where = where(scope, user, status, query, category, args);
    args.add(size);
    args.add(Math.max(0, page) * size);
    return jdbc.query(
        SELECT + where + " ORDER BY c.created_at DESC,c.id DESC LIMIT ? OFFSET ?",
        mapper,
        args.toArray());
  }

  public long count(String scope, Long user, String status, String query) {
    return count(scope, user, status, query, null);
  }

  public long count(String scope, Long user, String status, String query, Long category) {
    var args = new ArrayList<Object>();
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM complaints c " + where(scope, user, status, query, category, args),
        Long.class,
        args.toArray());
  }

  /** Reports still waiting on a department, oldest first, for service-level tracking. */
  public List<Complaint> openWork(String scope, Long user, int limit) {
    var args = new ArrayList<Object>();
    String where = where(scope, user, null, null, null, args);
    args.add(limit);
    return jdbc.query(
        SELECT
            + where
            + " AND c.status IN ('REPORTED','ASSIGNED','IN_PROGRESS') ORDER BY c.created_at,c.id"
            + " LIMIT ?",
        mapper,
        args.toArray());
  }

  /** SQL condition matching reports owned by a citizen, named or anonymous. */
  public String ownerCondition(String alias, long user, List<Object> args) {
    args.add(user);
    args.add(identity.hash(user));
    return "(" + alias + ".reporter_id=? OR " + alias + ".reporter_hash=?)";
  }

  private String where(
      String scope, Long user, String status, String query, Long category, List<Object> args) {
    String sql = "WHERE c.deleted_at IS " + ("trash".equals(scope) ? "NOT NULL " : "NULL ");
    if ("mine".equals(scope) || "trash".equals(scope)) {
      sql += "AND " + ownerCondition("c", user, args) + " ";
    } else if ("assigned".equals(scope)) {
      sql += "AND c.assigned_officer_id=? ";
      args.add(user);
    }
    if (status != null && !status.isBlank()) {
      try {
        ComplaintStatus.valueOf(status);
      } catch (IllegalArgumentException e) {
        throw FeatureException.invalid("Choose a valid status filter.");
      }
      sql += "AND c.status=? ";
      args.add(status);
    }
    if (category != null) {
      category(category);
      sql += "AND c.category_id=? ";
      args.add(category);
    }
    if (query != null && !query.isBlank()) {
      sql += "AND (LOWER(c.title) LIKE ? ESCAPE '!' OR LOWER(c.reference) LIKE ? ESCAPE '!') ";
      String q =
          query
              .strip()
              .toLowerCase(Locale.ROOT)
              .replace("!", "!!")
              .replace("%", "!%")
              .replace("_", "!_");
      q = "%" + q.substring(0, Math.min(120, q.length())) + "%";
      args.add(q);
      args.add(q);
    }
    return sql;
  }

  public Map<String, Long> totals(String scope, Long user) {
    var result = new LinkedHashMap<String, Long>();
    for (var s : ComplaintStatus.values()) result.put(s.name(), count(scope, user, s.name(), null));
    return result;
  }
}
