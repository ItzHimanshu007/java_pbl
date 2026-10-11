package com.civicpulse.insights;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.location.JaipurPlaces;
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
  private final ComplaintAccess access;
  private final ReporterIdentity identity;
  private final JaipurPlaces places;

  public InsightsService(
      JdbcTemplate jdbc,
      ComplaintDao dao,
      ComplaintAccess access,
      ReporterIdentity identity,
      JaipurPlaces places) {
    this.jdbc = jdbc;
    this.dao = dao;
    this.access = access;
    this.identity = identity;
    this.places = places;
  }

  // Each citizen counts once per issue. Every report carries the keyed reporter hash, so named
  // and anonymous reports by the same citizen are not counted twice.
  private static final String COMMUNITY =
      "SELECT"
          + " c.id,c.reference,c.title,c.address,c.latitude,c.longitude,c.status,c.reporter_id,"
          + "c.reporter_hash,c.category_id,c.created_at,COALESCE(c.issue_root_id,c.id) issue_root,"
          + "k.name category,k.code category_code,(SELECT COUNT(DISTINCT"
          + " COALESCE(d.reporter_hash,CONCAT('u',d.reporter_id))) FROM complaints d WHERE"
          + " COALESCE(d.issue_root_id,d.id)=COALESCE(c.issue_root_id,c.id) AND d.deleted_at IS"
          + " NULL AND d.status<>'REJECTED') repeats,(SELECT COUNT(*) FROM complaint_supports s"
          + " WHERE s.complaint_id=c.id) supports,(SELECT COUNT(*) FROM complaint_supports s WHERE"
          + " s.complaint_id=c.id AND s.citizen_id=?) supported FROM complaints c JOIN categories k"
          + " ON k.id=c.category_id ";

  private RowMapper<CommunityReport> mapper(
      CurrentUser user, double lat, double lon, boolean nearby) {
    String hash = identity.hash(user.id());
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
            user.role() == Role.CITIZEN
                && (r.getLong("reporter_id") == user.id() || hash.equals(r.getString("reporter_hash"))),
            nearby ? distance(lat, lon, r.getDouble("latitude"), r.getDouble("longitude")) : 0,
            r.getLong("category_id"),
            r.getLong("issue_root"),
            r.getLong("repeats"),
            priority(r.getString("status"), r.getLong("repeats")),
            progress(r.getString("status")),
            r.getString("category_code"),
            places.zoneOf(r.getDouble("latitude"), r.getDouble("longitude")),
            r.getTimestamp("created_at").toInstant().toString());
  }

  public List<CommunityReport> community(CurrentUser user) {
    return ranked(
        jdbc.query(
            COMMUNITY
                + "WHERE c.deleted_at IS NULL AND c.status<>'REJECTED' ORDER BY c.created_at"
                + " DESC,c.id DESC LIMIT 1000",
            mapper(user, 0, 0, false),
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
    return jdbc.query(sql, mapper(user, lat, lon, true), args.toArray()).stream()
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
    if (access.isOwner(c, user))
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
            mapper(u, 0, 0, false),
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

  /** Public progress for a reference number. Identities and notes are never included. */
  public Complaint track(String reference) {
    if (reference == null
        || reference.isBlank()
        || reference.length() > 40
        || !reference.strip().matches("[A-Za-z0-9-]+"))
      throw FeatureException.invalid("Enter a valid complaint reference number.");
    return dao.findByReference(reference);
  }

  public String scope(CurrentUser user) {
    return user.role() == Role.ADMIN ? "all" : user.role() == Role.OFFICER ? "assigned" : "mine";
  }

  public Map<String, Long> totals(CurrentUser user) {
    return dao.totals(scope(user), user.id());
  }

  /** SQL condition and arguments limiting complaints {@code c} to what a role may count. */
  private String scopeCondition(CurrentUser u, List<Object> args) {
    if (u.role() == Role.ADMIN) return "";
    if (u.role() == Role.OFFICER) {
      args.add(u.id());
      return " AND c.assigned_officer_id=?";
    }
    return " AND " + dao.ownerCondition("c", u.id(), args);
  }

  public Map<String, Long> categories(CurrentUser u) {
    var args = new ArrayList<Object>();
    String where = scopeCondition(u, args);
    var result = new LinkedHashMap<String, Long>();
    jdbc.query(
        "SELECT k.name,COUNT(c.id) amount FROM categories k LEFT JOIN complaints c ON"
            + " c.category_id=k.id AND c.deleted_at IS NULL"
            + where
            + " GROUP BY k.id,k.name ORDER BY k.id",
        (RowCallbackHandler) r -> result.put(r.getString("name"), r.getLong("amount")),
        args.toArray());
    return result;
  }

  /** Categories with reports, largest first, scaled for a bar chart. */
  public List<StatRow> categoryRows(CurrentUser u) {
    var args = new ArrayList<Object>();
    String where = scopeCondition(u, args);
    var rows = new ArrayList<StatRow>();
    jdbc.query(
        "SELECT k.name,k.code,COUNT(c.id) amount FROM categories k JOIN complaints c ON"
            + " c.category_id=k.id AND c.deleted_at IS NULL"
            + where
            + " GROUP BY k.id,k.name,k.code ORDER BY amount DESC,k.id",
        (RowCallbackHandler)
            r -> rows.add(new StatRow(r.getString("name"), r.getString("code"), r.getLong("amount"), 0)),
        args.toArray());
    return scaled(rows);
  }

  /** Reports per Jaipur service zone, largest first. */
  public List<StatRow> zoneRows(CurrentUser u) {
    var args = new ArrayList<Object>();
    String where = scopeCondition(u, args);
    var counts = new LinkedHashMap<String, Long>();
    jdbc.query(
        "SELECT c.latitude,c.longitude FROM complaints c WHERE c.deleted_at IS NULL" + where,
        (RowCallbackHandler)
            r -> counts.merge(places.zoneOf(r.getDouble(1), r.getDouble(2)), 1L, Long::sum),
        args.toArray());
    var rows = new ArrayList<StatRow>();
    counts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .forEach(e -> rows.add(new StatRow(e.getKey(), "zone", e.getValue(), 0)));
    return scaled(rows);
  }

  private static List<StatRow> scaled(List<StatRow> rows) {
    long max = rows.stream().mapToLong(StatRow::value).max().orElse(0);
    return rows.stream()
        .map(
            r ->
                new StatRow(
                    r.label(),
                    r.code(),
                    r.value(),
                    max == 0 ? 0 : (int) Math.max(2, Math.round(r.value() * 100.0 / max))))
        .toList();
  }

  /** Open work per department with overdue counts, for the administrator. */
  public List<DepartmentLoad> departmentLoad() {
    var open = new LinkedHashMap<String, long[]>();
    jdbc.query(
        "SELECT name FROM departments WHERE enabled=TRUE ORDER BY id",
        (RowCallbackHandler) r -> open.put(r.getString(1), new long[3]));
    for (var c : dao.openWork("all", null, 5000)) {
      var row = open.computeIfAbsent(c.department(), k -> new long[3]);
      row[0]++;
      if (c.isOverdue()) row[1]++;
    }
    jdbc.query(
        "SELECT d.name,COUNT(*) FROM complaints c JOIN departments d ON d.id=c.department_id"
            + " WHERE c.deleted_at IS NULL AND c.status='CLOSED' GROUP BY d.name",
        (RowCallbackHandler)
            r -> open.computeIfAbsent(r.getString(1), k -> new long[3])[2] = r.getLong(2));
    long max = open.values().stream().mapToLong(v -> v[0]).max().orElse(0);
    return open.entrySet().stream()
        .map(
            e ->
                new DepartmentLoad(
                    e.getKey(),
                    e.getValue()[0],
                    e.getValue()[1],
                    e.getValue()[2],
                    max == 0 ? 0 : (int) Math.round(e.getValue()[0] * 100.0 / max)))
        .sorted(Comparator.comparingLong(DepartmentLoad::open).reversed())
        .toList();
  }

  /** Reports past their target resolution time, most overdue first. */
  public List<Complaint> overdue(CurrentUser u, int limit) {
    return dao.openWork(scope(u), u.id(), 5000).stream()
        .filter(Complaint::isOverdue)
        .limit(limit)
        .toList();
  }

  public long overdueCount(CurrentUser u) {
    return dao.openWork(scope(u), u.id(), 5000).stream().filter(Complaint::isOverdue).count();
  }

  public PublicStats publicStats() {
    var totals = dao.totals("all", null);
    long awaiting = totals.get("REPORTED");
    long working = totals.get("ASSIGNED") + totals.get("IN_PROGRESS");
    long resolved = totals.get("RESOLVED") + totals.get("CLOSED");
    long total = awaiting + working + resolved;
    return new PublicStats(
        total,
        awaiting,
        working,
        resolved,
        total == 0 ? 0 : (int) Math.round(resolved * 100.0 / total),
        averageHours(null),
        dao.categories().size(),
        places.zones().size());
  }

  /** Mean hours from report to citizen-verified closure; {@code null} user means city-wide. */
  public String averageHours(CurrentUser u) {
    var args = new ArrayList<Object>();
    String where = u == null ? "" : scopeCondition(u, args);
    long[] values = {0, 0};
    jdbc.query(
        "SELECT c.created_at,c.closed_at FROM complaints c WHERE c.deleted_at IS NULL AND"
            + " c.status='CLOSED'"
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
        args.toArray());
    return values[1] == 0 ? "—" : String.format(Locale.ROOT, "%.1f", values[0] / 60.0 / values[1]);
  }

  public String csv(CurrentUser user, String status, String query) {
    var rows = dao.list(scope(user), user.id(), status, query, 0, 1000);
    StringBuilder text =
        new StringBuilder(
            "Reference,Title,Category,Department,Status,Zone,Address,Latitude,Longitude,Created"
                + " UTC,Updated UTC,Due UTC,Overdue,Anonymous\r\n");
    for (var c : rows)
      text.append(
              String.join(
                  ",",
                  List.of(
                          c.reference(),
                          c.title(),
                          c.category(),
                          c.department(),
                          c.status().name(),
                          places.zoneOf(c.latitude(), c.longitude()),
                          c.address(),
                          Double.toString(c.latitude()),
                          Double.toString(c.longitude()),
                          c.createdAt().toString(),
                          c.updatedAt().toString(),
                          c.dueAt().toString(),
                          c.isOverdue() ? "Yes" : "No",
                          c.anonymous() ? "Yes" : "No")
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
