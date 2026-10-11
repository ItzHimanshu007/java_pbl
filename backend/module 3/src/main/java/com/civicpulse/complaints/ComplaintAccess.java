package com.civicpulse.complaints;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.shared.*;
import org.springframework.stereotype.Component;

@Component
public class ComplaintAccess {
  private final ReporterIdentity identity;

  public ComplaintAccess(ReporterIdentity identity) {
    this.identity = identity;
  }

  /** The citizen who reported this, named or anonymous. */
  public boolean isOwner(Complaint c, CurrentUser u) {
    return u != null
        && u.role() == Role.CITIZEN
        && (c.reporterId() == u.id() || identity.matches(c.reporterHash(), u.id()));
  }

  /**
   * Account recorded for the reporter's own actions. Anonymous reports keep every citizen action
   * under the anonymous account so the timeline never reveals who reported.
   */
  public long actor(Complaint c, CurrentUser u) {
    return c.anonymous() && isOwner(c, u) ? identity.anonymousAccount() : u.id();
  }

  public void read(Complaint c, CurrentUser u) {
    if (u == null
        || !(u.role() == Role.ADMIN
            || isOwner(c, u)
            || u.role() == Role.OFFICER && Long.valueOf(u.id()).equals(c.assignedOfficerId())))
      throw FeatureException.forbidden();
  }

  public void owner(Complaint c, CurrentUser u) {
    if (!isOwner(c, u)) throw FeatureException.forbidden();
  }

  public void officer(Complaint c, CurrentUser u) {
    if (u.role() != Role.OFFICER || !Long.valueOf(u.id()).equals(c.assignedOfficerId()))
      throw FeatureException.forbidden();
  }
}
