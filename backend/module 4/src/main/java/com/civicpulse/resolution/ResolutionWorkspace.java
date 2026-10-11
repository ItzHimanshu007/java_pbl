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

  public ResolutionWorkspace(ComplaintDao dao) {
    this.dao = dao;
  }

  public Role role() {
    return Role.OFFICER;
  }

  public List<String> scripts() {
    return List.of("/assets/js/resolution.js");
  }

  public String render(CurrentUser u, Model m) {
    m.addAttribute("totals", dao.totals("assigned", u.id()));
    m.addAttribute("reports", dao.list("assigned", u.id(), null, null, 0, 5));
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
    m.addAttribute(
        "canVerify",
        u.role() == Role.CITIZEN
            && c.reporterId() == u.id()
            && c.status() == ComplaintStatus.RESOLVED);
    m.addAttribute("resolutionInstalled", true);
  }
}
