package com.civicpulse.complaints;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.shared.*;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionService {
  private final ComplaintDao dao;
  private final ImageStore images;
  private final ComplaintAccess access;

  public SubmissionService(ComplaintDao dao, ImageStore images, ComplaintAccess access) {
    this.dao = dao;
    this.images = images;
    this.access = access;
  }

  @Transactional
  public long submit(ComplaintForm f, MultipartFile photo, CurrentUser user) {
    if (user.role() != Role.CITIZEN) throw FeatureException.forbidden();
    validate(f);
    byte[] image = images.validate(photo);
    String reference =
        "CP-"
            + LocalDate.now(ZoneOffset.UTC).getYear()
            + "-"
            + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase(java.util.Locale.ROOT);
    Long root = null;
    if (f.getRelatedReportId() != null) {
      var related = dao.lock(f.getRelatedReportId());
      if (related.categoryId() != f.getCategoryId()
          || related.status() == ComplaintStatus.CLOSED
          || related.status() == ComplaintStatus.REJECTED
          || com.civicpulse.insights.InsightsService.distance(
                  f.getLatitude(), f.getLongitude(), related.latitude(), related.longitude())
              > 150)
        throw FeatureException.invalid(
            "Choose an active report of the same issue within 150 metres.");
      root = dao.issueRoot(related.id());
    }
    long id = dao.create(f, user.id(), reference);
    if (root != null) dao.link(id, root);
    images.add(id, user.id(), "ORIGINAL", image);
    dao.event(
        id, user.id(), "REPORTED", null, ComplaintStatus.REPORTED, "Report submitted for review.");
    return id;
  }

  public void edit(long id, ComplaintForm f, CurrentUser user) {
    access.owner(dao.find(id), user);
    throw FeatureException.conflict(
        "Submitted reports cannot be edited. Delete the report and submit a new one.");
  }

  @Transactional
  public void delete(long id, CurrentUser user) {
    var c = dao.lock(id);
    access.owner(c, user);
    dao.softDelete(id);
    dao.event(
        id,
        user.id(),
        "DELETED",
        c.status(),
        c.status(),
        "Report moved to deleted reports by its citizen.");
  }

  @Transactional
  public void restore(long id, CurrentUser user) {
    var c = dao.lockDeleted(id);
    access.owner(c, user);
    dao.restore(id);
    dao.event(id, user.id(), "RESTORED", c.status(), c.status(), "Report restored by its citizen.");
  }

  private void validate(ComplaintForm f) {
    if (f.getTitle() == null
        || f.getTitle().strip().length() < 5
        || f.getTitle().length() > 120
        || f.getDescription() == null
        || f.getDescription().strip().length() < 15
        || f.getDescription().length() > 2000
        || f.getAddress() == null
        || f.getAddress().strip().length() < 5
        || f.getAddress().length() > 250
        || f.getLatitude() == null
        || f.getLongitude() == null
        || !Double.isFinite(f.getLatitude())
        || !Double.isFinite(f.getLongitude())
        || Math.abs(f.getLatitude()) > 90
        || Math.abs(f.getLongitude()) > 180
        || f.getCategoryId() == null)
      throw FeatureException.invalid("Check the report details and location.");
    dao.category(f.getCategoryId());
  }
}
