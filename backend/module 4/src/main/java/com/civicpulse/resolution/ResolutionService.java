package com.civicpulse.resolution;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResolutionService {
  private final ComplaintDao dao;
  private final ComplaintAccess access;
  private final ImageStore images;

  public ResolutionService(ComplaintDao dao, ComplaintAccess access, ImageStore images) {
    this.dao = dao;
    this.access = access;
    this.images = images;
  }

  private String note(String value) {
    if (value == null || value.strip().length() < 5 || value.length() > 1000)
      throw FeatureException.invalid("Add an update between 5 and 1,000 characters.");
    return value.strip();
  }

  @Transactional
  public void start(long id, String value, CurrentUser user) {
    var c = dao.lock(id);
    access.officer(c, user);
    require(c, ComplaintStatus.ASSIGNED);
    String n = note(value);
    dao.transition(id, ComplaintStatus.IN_PROGRESS, c.assignedOfficerId());
    dao.event(id, user.id(), "WORK_STARTED", c.status(), ComplaintStatus.IN_PROGRESS, n);
  }

  @Transactional
  public void progress(long id, String value, CurrentUser user) {
    var c = dao.lock(id);
    access.officer(c, user);
    require(c, ComplaintStatus.IN_PROGRESS);
    dao.event(id, user.id(), "PROGRESS_UPDATE", c.status(), c.status(), note(value));
  }

  @Transactional
  public void resolve(long id, String value, MultipartFile photo, CurrentUser user) {
    var c = dao.lock(id);
    access.officer(c, user);
    require(c, ComplaintStatus.IN_PROGRESS);
    String n = note(value);
    byte[] image = images.validate(photo);
    images.add(id, user.id(), "REPAIR", image);
    dao.transition(id, ComplaintStatus.RESOLVED, c.assignedOfficerId());
    dao.event(id, user.id(), "REPAIR_SUBMITTED", c.status(), ComplaintStatus.RESOLVED, n);
  }

  @Transactional
  public void confirm(long id, CurrentUser user) {
    var c = dao.lock(id);
    access.owner(c, user);
    require(c, ComplaintStatus.RESOLVED);
    if (!images.hasRepair(id))
      throw FeatureException.conflict("Repair evidence is required before confirmation.");
    dao.transition(id, ComplaintStatus.CLOSED, c.assignedOfficerId());
    dao.event(
        id,
        access.actor(c, user),
        "CITIZEN_CONFIRMED",
        c.status(),
        ComplaintStatus.CLOSED,
        "Citizen verified the repair and closed the report.");
  }

  @Transactional
  public void reopen(long id, String value, CurrentUser user) {
    var c = dao.lock(id);
    access.owner(c, user);
    require(c, ComplaintStatus.RESOLVED);
    String n = note(value);
    dao.transition(id, ComplaintStatus.IN_PROGRESS, c.assignedOfficerId());
    dao.event(id, access.actor(c, user), "REOPENED", c.status(), ComplaintStatus.IN_PROGRESS, n);
  }

  private void require(Complaint c, ComplaintStatus expected) {
    if (c.status() != expected)
      throw FeatureException.conflict(
          "This action is not available for the report's current status. Reload the report to see"
              + " the latest update.");
  }
}
