package com.civicpulse.shared;

import com.civicpulse.account.*;
import com.civicpulse.auth.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ViewContext {
  private final Environment environment;
  private final ObjectProvider<NavigationContributor> navigation;
  private final ProfileDao profiles;
  private final UiText text;

  public ViewContext(
      Environment environment,
      ObjectProvider<NavigationContributor> navigation,
      ProfileDao profiles,
      UiText text) {
    this.environment = environment;
    this.navigation = navigation;
    this.profiles = profiles;
    this.text = text;
  }

  @ModelAttribute
  public void context(
      @AuthenticationPrincipal CurrentUser user, Model model, HttpServletRequest request) {
    String language = "en", theme = "system";
    if (request.getCookies() != null)
      for (var c : request.getCookies()) {
        if (c.getName().equals("civic_language") && c.getValue().equals("hi")) language = "hi";
        if (c.getName().equals("civic_theme")
            && java.util.List.of("dark", "light", "system").contains(c.getValue()))
          theme = c.getValue();
      }
    if (user != null) {
      var p = profiles.find(user.id());
      language = p.language();
      theme = p.theme();
    }
    String query = request.getQueryString();
    String path = request.getRequestURI().substring(request.getContextPath().length());
    model.addAttribute("currentPath", query == null ? path : path + "?" + query);
    model.addAttribute("language", language);
    model.addAttribute("theme", theme);
    model.addAttribute("ui", text.forLanguage(language));
    model.addAttribute("signedIn", user != null);
    model.addAttribute("demoMode", environment.acceptsProfiles(Profiles.of("demo")));
    model.addAttribute(
        "moduleStyles", navigation.orderedStream().flatMap(n -> n.styles().stream()).toList());
    model.addAttribute(
        "moduleScripts", navigation.orderedStream().flatMap(n -> n.scripts().stream()).toList());
    if (user != null) {
      model.addAttribute("displayName", user.displayName());
      model.addAttribute("accountEmail", user.email());
      model.addAttribute("roleLabel", user.role().label());
      model.addAttribute("isCitizen", user.role() == Role.CITIZEN);
      model.addAttribute("isAdmin", user.role() == Role.ADMIN);
      model.addAttribute("dashboardPath", user.role().dashboard());
      model.addAttribute(
          "navigationLinks",
          navigation.orderedStream().flatMap(n -> n.links(user.role()).stream()).toList());
    }
  }
}
