package com.civicpulse.insights;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import java.time.Duration;
import java.util.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InsightsService {
  private final JdbcTemplate jdbc;
  private final ComplaintDao dao;

  public InsightsService(JdbcTemplate jdbc, ComplaintDao dao) {
    this.jdbc = jdbc;
    this.dao = dao;
  }

  private static final String COMMUNITY =
      "SELECT"
          + " c.id,c.reference,c.title,c.address,c.latitude,c.longitude,c.status,c.reporter_id,c.category_id,COALESCE(c.issue_root_id,c.id)"
          + " issue_root,k.name category,(SELECT COUNT(DISTINCT d.reporter_id) FROM complaints d"
          + " WHERE COALESCE(d.issue_root_id,d.id)=COALESCE(c.issue_root_id,c.id) AND d.deleted_at"
          + " IS NULL AND d.status<>'REJECTED') repeats,(SELECT COUNT(*) FROM complaint_supports s"
          + " WHERE s.complaint_id=c.id) supports,(SELECT COUNT(*) FROM complaint_supports s WHERE"
          + " s.complaint_id=c.id AND s.citizen_id=?) supported FROM complaints c JOIN categories k"
          + " ON k.id=c.category_id ";

  private RowMapper<CommunityReport> mapper(long user, double lat, double lon, boolean nearby) {
    return (r, n) ->
        new CommunityReport(
            r.getLong("id"),
            r.getString("reference"),
            r.getString("title"),
            r.getString("category"),
            r.getString("address"),
            r.getDouble("latitude"),
            r.getDouble("longitude"),
            r.getString("status"),
            r.getLong("supports"),
            r.getLong("supported") > 0,
            r.getLong("reporter_id") == user,
            nearby ? distance(lat, lon, r.getDouble("latitude"), r.getDouble("longitude")) : 0,
            r.getLong("category_id"),
            r.getLong("issue_root"),
            r.getLong("repeats"),
            priority(r.getString("status"), r.getLong("repeats")),
            progress(r.getString("status")));
  }

  public List<CommunityReport> community(CurrentUser user) {
    return ranked(
        jdbc.query(
            COMMUNITY
                + "WHERE c.deleted_at IS NULL AND c.status<>'REJECTED' ORDER BY c.created_at"
                + " DESC,c.id DESC LIMIT 1000",
            mapper(user.id(), 0, 0, false),
            user.id()));
  }

  public List<CommunityReport> nearby(long category, double lat, double lon, CurrentUser user) {
    if (!Double.isFinite(lat) || !Double.isFinite(lon) || Math.abs(lat) > 90 || Math.abs(lon) > 180)
      throw FeatureException.invalid("Choose a valid location.");
    dao.category(category);
    double deltaLat = Math.toDegrees(250.0 / 6_371_000);
    double deltaLon =
        Math.abs(lat) + deltaLat >= 90
            ? 180
            : Math.toDegrees(
                Math.asin(
                    Math.min(1, Math.sin(250.0 / 6_371_000) / Math.cos(Math.toRadians(lat)))));
    double min = lon - deltaLon, max = lon + deltaLon;
    String sql =
        COMMUNITY
            + "WHERE c.deleted_at IS NULL AND c.category_id=? AND c.status IN"
            + " ('REPORTED','ASSIGNED','IN_PROGRESS','RESOLVED') AND c.latitude BETWEEN ? AND ? ";
    var args = new ArrayList<Object>(List.of(user.id(), category, lat - deltaLat, lat + deltaLat));
    if (deltaLon < 180) {
      if (min < -180) {
        sql += "AND (c.longitude>=? OR c.longitude<=?) ";
        args.add(min + 360);
        args.add(max);
      } else if (max > 180) {
        sql += "AND (c.longitude>=? OR c.longitude<=?) ";
        args.add(min);
        args.add(max - 360);
      } else {
        sql += "AND c.longitude BETWEEN ? AND ? ";
        args.add(min);
        args.add(max);
      }
    }
    return jdbc.query(sql, mapper(user.id(), lat, lon, true), args.toArray()).stream()
        .filter(c -> c.distanceMeters() <= 250)
        .sorted(
            Comparator.comparingDouble(CommunityReport::distanceMeters)
                .thenComparingLong(CommunityReport::id))
        .limit(5)
        .toList();
  }

  public static double distance(double lat1, double lon1, double lat2, double lon2) {
    double a =
        Math.pow(Math.sin(Math.toRadians(lat2 - lat1) / 2), 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(Math.toRadians(lon2 - lon1) / 2), 2);
    a = Math.max(0, Math.min(1, a));
    return 6_371_000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  @Transactional
  public void support(long id, CurrentUser user) {
    if (user.role() != Role.CITIZEN) throw FeatureException.forbidden();
    var c = dao.lock(id);
    if (c.reporterId() == user.id())
      throw FeatureException.conflict("You are already the reporter of this issue.");
    if (c.status() == ComplaintStatus.CLOSED || c.status() == ComplaintStatus.REJECTED)
      throw FeatureException.conflict("Only active issues can receive support.");
    if (jdbc.queryForObject(
            "SELECT COUNT(*) FROM complaint_supports WHERE complaint_id=? AND citizen_id=?",
            Long.class,
            id,
            user.id())
        == 0)
      jdbc.update(
          "INSERT INTO complaint_supports(complaint_id,citizen_id) VALUES(?,?)", id, user.id());
  }

  public static String priority(String status, long count) {
    if (status.equals("CLOSED")) return "low";
    return count >= 5 ? "high" : count >= 2 ? "medium" : "low";
  }

  public static int progress(String status) {
    return switch (status) {
      case "ASSIGNED" -> 25;
      case "IN_PROGRESS" -> 50;
      case "RESOLVED" -> 85;
      case "CLOSED" -> 100;
      default -> 0;
    };
  }

  private List<CommunityReport> ranked(List<CommunityReport> rows) {
    return rows.stream()
        .sorted(
            Comparator.comparingInt((CommunityReport c) -> c.status().equals("CLOSED") ? 1 : 0)
                .thenComparing(Comparator.comparingLong(CommunityReport::repeatCount).reversed())
                .thenComparing(Comparator.comparingLong(CommunityReport::supports).reversed())
                .thenComparing(Comparator.comparingLong(CommunityReport::id).reversed()))
        .toList();
  }

  public List<CommunityReport> feed(
      CurrentUser u, Double lat, Double lon, int radius, boolean excludeMine) {
    if ((lat == null) != (lon == null)
        || lat != null
            && (!Double.isFinite(lat)
                || !Double.isFinite(lon)
                || Math.abs(lat) > 90
                || Math.abs(lon) > 180)
        || radius < 1
        || radius > 50) throw FeatureException.invalid("Choose a valid location and area.");
    var rows =
        community(u).stream()
            .filter(c -> !excludeMine || !c.mine())
            .filter(
                c ->
                    lat == null || distance(lat, lon, c.latitude(), c.longitude()) <= radius * 1000)
            .toList();
    var issues = new LinkedHashMap<Long, CommunityReport>();
    for (var c : rows) issues.putIfAbsent(c.issueRootId(), c);
    return issues.values().stream().limit(200).toList();
  }

  public CommunityReport summary(long id, CurrentUser u) {
    var rows =
        jdbc.query(
            COMMUNITY + "WHERE c.id=? AND c.deleted_at IS NULL AND c.status<>'REJECTED'",
            mapper(u.id(), 0, 0, false),
            u.id(),
            id);
    if (rows.isEmpty()) throw FeatureException.missing();
    return rows.get(0);
  }

  public List<java.util.Map<String, String>> publicUpdates(long id) {
    return jdbc.query(
        "SELECT action,to_status,created_at FROM complaint_events WHERE complaint_id=? AND action"
            + " NOT IN ('DELETED','RESTORED') ORDER BY id",
        (r, n) ->
            Map.of(
                "action",
                r.getString("action"),
                "status",
                r.getString("to_status"),
                "createdAt",
                r.getTimestamp("created_at").toInstant().toString()),
        id);
  }

  public String scope(CurrentUser user) {
    return user.role() == Role.ADMIN ? "all" : user.role() == Role.OFFICER ? "assigned" : "mine";
  }

  public Map<String, Long> totals(CurrentUser user) {
    return dao.totals(scope(user), user.id());
  }

  public Map<String, Long> categories(CurrentUser u) {
    String where =
        u.role() == Role.ADMIN
            ? ""
            : u.role() == Role.CITIZEN ? " AND c.reporter_id=?" : " AND c.assigned_officer_id=?";
    var result = new LinkedHashMap<String, Long>();
    jdbc.query(
        "SELECT k.name,COUNT(c.id) amount FROM categories k LEFT JOIN complaints c ON"
            + " c.category_id=k.id AND c.deleted_at IS NULL"
            + where
            + " GROUP BY k.id,k.name ORDER BY k.id",
        (RowCallbackHandler) r -> result.put(r.getString("name"), r.getLong("amount")),
        u.role() == Role.ADMIN ? new Object[0] : new Object[] {u.id()});
    return result;
  }

  public String averageHours(CurrentUser u) {
    String where =
        u.role() == Role.ADMIN
            ? ""
            : u.role() == Role.CITIZEN ? " AND reporter_id=?" : " AND assigned_officer_id=?";
    long[] values = {0, 0};
    jdbc.query(
        "SELECT created_at,closed_at FROM complaints WHERE deleted_at IS NULL AND status='CLOSED'"
            + where,
        (RowCallbackHandler)
            r -> {
              values[0] +=
                  Math.max(
                      0,
                      Duration.between(r.getTimestamp(1).toInstant(), r.getTimestamp(2).toInstant())
                          .toMinutes());
              values[1]++;
            },
        u.role() == Role.ADMIN ? new Object[0] : new Object[] {u.id()});
    return values[1] == 0 ? "—" : String.format(Locale.ROOT, "%.1f", values[0] / 60.0 / values[1]);
  }

  public String csv(CurrentUser user, String status, String query) {
    var rows = dao.list(scope(user), user.id(), status, query, 0, 1000);
    StringBuilder text =
        new StringBuilder(
            "Reference,Title,Category,Status,Address,Latitude,Longitude,Created UTC,Updated"
                + " UTC\r\n");
    for (var c : rows)
      text.append(
              String.join(
                  ",",
                  List.of(
                          c.reference(),
                          c.title(),
                          c.category(),
                          c.status().name(),
                          c.address(),
                          Double.toString(c.latitude()),
                          Double.toString(c.longitude()),
                          c.createdAt().toString(),
                          c.updatedAt().toString())
                      .stream()
                      .map(InsightsService::cell)
                      .toList()))
          .append("\r\n");
    return text.toString();
  }

  public static String cell(String input) {
    String v = input == null ? "" : input;
    String stripped = v.stripLeading();
    if (!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0
        || v.startsWith("\t")
        || v.startsWith("\r")
        || v.startsWith("\n")) v = "'" + v;
    return "\"" + v.replace("\"", "\"\"") + "\"";
  }
}
