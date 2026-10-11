package com.civicpulse.complaints;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.shared.*;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@Order(10)
public class ReportingWorkspace implements WorkspaceFeature, NavigationContributor {
  private final ComplaintDao dao;

  public ReportingWorkspace(ComplaintDao dao) {
    this.dao = dao;
  }

  public Role role() {
    return Role.CITIZEN;
  }

  public List<String> styles() {
    return List.of();
  }

  public String render(CurrentUser u, Model m) {
    m.addAttribute("totals", dao.totals("mine", u.id()));
    m.addAttribute("reports", dao.list("mine", u.id(), null, null, 0, 5));
    return "redirect:/feed";
  }

  public List<NavigationLink> links(Role r) {
    return r == Role.CITIZEN
        ? List.of(
            new NavigationLink("My reports", "/citizen/reports"),
            new NavigationLink("Report an issue", "/citizen/reports/new"))
        : List.of();
  }
}
