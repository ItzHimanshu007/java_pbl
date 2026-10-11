package com.civicpulse.shared;

import com.civicpulse.account.ProfileDao;
import com.civicpulse.auth.CurrentUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/** Header language and theme switch, available before and after signing in. */
@Controller
public class PreferenceController {
  private final ProfileDao profiles;

  public PreferenceController(ProfileDao profiles) {
    this.profiles = profiles;
  }

  @PostMapping("/preferences")
  public String update(
      @AuthenticationPrincipal CurrentUser user,
      @RequestParam(required = false) String language,
      @RequestParam(required = false) String theme,
      @RequestParam(defaultValue = "/") String next,
      HttpServletResponse response) {
    String lang = language != null && language.matches("en|hi") ? language : null;
    String look = theme != null && theme.matches("light|dark|system") ? theme : null;
    if (user != null && (lang != null || look != null))
      profiles.savePreferences(user.id(), lang, look);
    if (lang != null) cookie(response, "civic_language", lang);
    if (look != null) cookie(response, "civic_theme", look);
    return "redirect:" + safePath(next);
  }

  /** Only same-site relative paths; anything else returns to the home page. */
  static String safePath(String next) {
    if (next == null
        || next.length() > 500
        || !next.startsWith("/")
        || next.startsWith("//")
        || next.startsWith("/\\")
        || next.chars().anyMatch(c -> c < 0x20 || c == 0x7f)) return "/";
    return next;
  }

  private static void cookie(HttpServletResponse response, String name, String value) {
    response.addHeader(
        "Set-Cookie",
        ResponseCookie.from(name, value)
            .path("/")
            .sameSite("Lax")
            .maxAge(31536000)
            .httpOnly(true)
            .build()
            .toString());
  }
}
