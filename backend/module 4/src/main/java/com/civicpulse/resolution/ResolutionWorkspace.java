package com.civicpulse.resolution;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@Order(30)
public class ResolutionWorkspace
    implements WorkspaceFeature, NavigationContributor, DetailContributor {
  private final ComplaintDao dao;
  private final ComplaintAccess access;
  private final com.civicpulse.insights.InsightsService insights;

  public ResolutionWorkspace(
      ComplaintDao dao, ComplaintAccess access, com.civicpulse.insights.InsightsService insights) {
    this.dao = dao;
    this.access = access;
    this.insights = insights;
  }

  public Role role() {
    return Role.OFFICER;
  }

  public List<String> scripts() {
    return List.of("/assets/js/resolution.js");
  }

  public String render(CurrentUser u, Model m) {
    m.addAttribute("totals", dao.totals("assigned", u.id()));
    m.addAttribute("reports", dao.list("assigned", u.id(), null, null, 0, 6));
    m.addAttribute("overdueReports", insights.overdue(u, 5));
    m.addAttribute("overdueCount", insights.overdueCount(u));
    m.addAttribute("averageHours", insights.averageHours(u));
    return "resolution/dashboard";
  }

  public List<NavigationLink> links(Role r) {
    return r == Role.OFFICER
        ? List.of(new NavigationLink("My work", "/officer/reports"))
        : List.of();
  }

  public void add(Complaint c, CurrentUser u, Model m) {
    boolean assigned =
        u.role() == Role.OFFICER && Long.valueOf(u.id()).equals(c.assignedOfficerId());
    m.addAttribute("canStart", assigned && c.status() == ComplaintStatus.ASSIGNED);
    m.addAttribute("canProgress", assigned && c.status() == ComplaintStatus.IN_PROGRESS);
    m.addAttribute("canVerify", access.isOwner(c, u) && c.status() == ComplaintStatus.RESOLVED);
    m.addAttribute("resolutionInstalled", true);
  }
}
