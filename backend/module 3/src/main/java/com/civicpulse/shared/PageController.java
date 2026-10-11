package com.civicpulse.shared;

import com.civicpulse.auth.CurrentUser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
  private final ObjectProvider<WorkspaceFeature> features;
  private final ObjectProvider<HomeContributor> home;

  public PageController(
      ObjectProvider<WorkspaceFeature> features, ObjectProvider<HomeContributor> home) {
    this.features = features;
    this.home = home;
  }

  @GetMapping("/")
  public String home(Model model) {
    home.orderedStream().forEach(h -> h.add(model));
    return "home";
  }

  @GetMapping("/dashboard")
  public String dashboard(@AuthenticationPrincipal CurrentUser user) {
    return "redirect:" + user.role().dashboard();
  }

  @GetMapping({"/citizen/dashboard", "/officer/dashboard", "/admin/dashboard"})
  public String workspace(@AuthenticationPrincipal CurrentUser user, Model model) {
    return features
        .orderedStream()
        .filter(f -> f.role() == user.role())
        .findFirst()
        .map(f -> f.render(user, model))
        .orElse("account/dashboard");
  }
}
