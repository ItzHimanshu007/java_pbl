package com.civicpulse.administration;

import com.civicpulse.account.UserDao;
import com.civicpulse.auth.*;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdministrationService {
  private final JdbcTemplate jdbc;
  private final ComplaintDao dao;
  private final UserDao users;
  private final PasswordEncoder passwords;

  public AdministrationService(
      JdbcTemplate jdbc, ComplaintDao dao, UserDao users, PasswordEncoder passwords) {
    this.jdbc = jdbc;
    this.dao = dao;
    this.users = users;
    this.passwords = passwords;
  }

  public List<Department> departments() {
    return jdbc.query(
        "SELECT id,name FROM departments WHERE enabled=TRUE ORDER BY id",
        (r, n) -> new Department(r.getLong("id"), r.getString("name")));
  }

  public List<Officer> officers() {
    return jdbc.query(
        "SELECT u.*,d.name department FROM users u JOIN departments d ON d.id=u.department_id WHERE"
            + " u.role='OFFICER' ORDER BY u.display_name,u.id",
        (r, n) ->
            new Officer(
                r.getLong("id"),
                r.getString("display_name"),
                r.getString("email"),
                r.getLong("department_id"),
                r.getString("department"),
                r.getBoolean("enabled")));
  }

  public long activeAssignments(long officer) {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM complaints WHERE deleted_at IS NULL AND assigned_officer_id=? AND"
            + " status IN ('ASSIGNED','IN_PROGRESS','RESOLVED')",
        Long.class,
        officer);
  }

  @Transactional
  public void createOfficer(OfficerForm f, CurrentUser user) {
    admin(user);
    AuthService.checkPassword(f.getPassword());
    checkDepartment(f.getDepartmentId());
    var created =
        users.create(
            f.getDisplayName().strip(),
            AuthService.normalizeEmail(f.getEmail()),
            passwords.encode(f.getPassword()),
            Role.OFFICER);
    jdbc.update("UPDATE users SET department_id=? WHERE id=?", f.getDepartmentId(), created.id());
    audit(user.id(), "OFFICER_CREATED", created.id(), "Officer account created.");
  }

  @Transactional
  public void setEnabled(long officer, boolean enabled, CurrentUser user) {
    admin(user);
    lockOfficer(officer);
    if (!enabled && activeAssignments(officer) > 0)
      throw FeatureException.conflict(
          "This officer has active reports. Complete or reassign their work before disabling the"
              + " account.");
    jdbc.update("UPDATE users SET enabled=? WHERE id=?", enabled, officer);
    audit(
        user.id(),
        enabled ? "OFFICER_ENABLED" : "OFFICER_DISABLED",
        officer,
        "Officer access updated.");
  }

  @Transactional
  public void assign(long id, long officer, long department, String note, CurrentUser user) {
    admin(user);
    var c = dao.lock(id);
    if (c.status() != ComplaintStatus.REPORTED && c.status() != ComplaintStatus.ASSIGNED)
      throw FeatureException.conflict(
          "Only reported or assigned issues can be assigned or reassigned.");
    checkDepartment(department);
    var o = lockOfficer(officer);
    if (!o.enabled() || o.departmentId() != department)
      throw FeatureException.invalid("Choose an enabled officer from the selected department.");
    String reason = reason(note);
    jdbc.update("UPDATE complaints SET department_id=? WHERE id=?", department, id);
    dao.transition(id, ComplaintStatus.ASSIGNED, officer);
    dao.event(
        id,
        user.id(),
        c.status() == ComplaintStatus.ASSIGNED ? "REASSIGNED" : "ASSIGNED",
        c.status(),
        ComplaintStatus.ASSIGNED,
        reason + " Assigned to " + o.name() + ".");
  }

  @Transactional
  public void reject(long id, String note, CurrentUser user) {
    admin(user);
    var c = dao.lock(id);
    if (c.status() != ComplaintStatus.REPORTED)
      throw FeatureException.conflict("Only a report awaiting review can be rejected.");
    String reason = reason(note);
    dao.transition(id, ComplaintStatus.REJECTED, null);
    dao.event(id, user.id(), "REJECTED", c.status(), ComplaintStatus.REJECTED, reason);
  }

  private Officer lockOfficer(long id) {
    var rows =
        jdbc.query(
            "SELECT u.*,d.name department FROM users u JOIN departments d ON d.id=u.department_id"
                + " WHERE u.id=? AND u.role='OFFICER' FOR UPDATE",
            (r, n) ->
                new Officer(
                    r.getLong("id"),
                    r.getString("display_name"),
                    r.getString("email"),
                    r.getLong("department_id"),
                    r.getString("department"),
                    r.getBoolean("enabled")),
            id);
    return rows.stream()
        .findFirst()
        .orElseThrow(() -> FeatureException.invalid("Choose a valid officer."));
  }

  private void checkDepartment(Long id) {
    if (id == null || departments().stream().noneMatch(d -> d.id() == id))
      throw FeatureException.invalid("Choose an enabled department.");
  }

  private static void admin(CurrentUser u) {
    if (u.role() != Role.ADMIN) throw FeatureException.forbidden();
  }

  public static String reason(String v) {
    if (v == null || v.strip().length() < 5 || v.length() > 1000)
      throw FeatureException.invalid("Add a reason or update between 5 and 1,000 characters.");
    return v.strip();
  }

  private void audit(long actor, String action, long subject, String note) {
    jdbc.update(
        "INSERT INTO administration_events(actor_id,action,subject_id,note) VALUES(?,?,?,?)",
        actor,
        action,
        subject,
        note);
  }
}
