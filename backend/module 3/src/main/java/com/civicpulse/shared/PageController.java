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

  public PageController(ObjectProvider<WorkspaceFeature> features) {
    this.features = features;
  }

  @GetMapping("/")
  public String home() {
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
