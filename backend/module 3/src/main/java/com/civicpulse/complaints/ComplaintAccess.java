package com.civicpulse.complaints;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.shared.*;
import org.springframework.stereotype.Component;

@Component
public class ComplaintAccess {
  public void read(Complaint c, CurrentUser u) {
    if (u == null
        || !(u.role() == Role.ADMIN
            || u.role() == Role.CITIZEN && c.reporterId() == u.id()
            || u.role() == Role.OFFICER && Long.valueOf(u.id()).equals(c.assignedOfficerId())))
      throw FeatureException.forbidden();
  }

  public void owner(Complaint c, CurrentUser u) {
    if (u.role() != Role.CITIZEN || c.reporterId() != u.id()) throw FeatureException.forbidden();
  }

  public void officer(Complaint c, CurrentUser u) {
    if (u.role() != Role.OFFICER || !Long.valueOf(u.id()).equals(c.assignedOfficerId()))
      throw FeatureException.forbidden();
  }
}
