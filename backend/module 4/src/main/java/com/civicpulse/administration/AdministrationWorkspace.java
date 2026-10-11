package com.civicpulse.administration;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@Order(20)
public class AdministrationWorkspace
    implements WorkspaceFeature, NavigationContributor, DetailContributor {
  private final ComplaintDao dao;
  private final AdministrationService service;

  public AdministrationWorkspace(ComplaintDao dao, AdministrationService service) {
    this.dao = dao;
    this.service = service;
  }

  public Role role() {
    return Role.ADMIN;
  }

  public String render(CurrentUser u, Model m) {
    m.addAttribute("totals", dao.totals("all", null));
    m.addAttribute("reports", dao.list("all", null, "REPORTED", null, 0, 5));
    return "administration/dashboard";
  }

  public List<NavigationLink> links(Role r) {
    return r == Role.ADMIN
        ? List.of(
            new NavigationLink("All reports", "/admin/reports"),
            new NavigationLink("Officers", "/admin/officers"))
        : List.of();
  }

  public void add(Complaint c, CurrentUser u, Model m) {
    if (u.role() == Role.ADMIN
        && (c.status() == ComplaintStatus.REPORTED || c.status() == ComplaintStatus.ASSIGNED)) {
      m.addAttribute("canAssign", true);
      m.addAttribute("canReject", c.status() == ComplaintStatus.REPORTED);
      m.addAttribute("officers", service.officers().stream().filter(Officer::enabled).toList());
      m.addAttribute("departments", service.departments());
    }
  }
}
