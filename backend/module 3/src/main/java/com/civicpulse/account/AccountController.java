package com.civicpulse.account;

import com.civicpulse.auth.CurrentUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController {
  private final UserDao users;
  private final ProfileDao profiles;

  public AccountController(UserDao users, ProfileDao profiles) {
    this.users = users;
    this.profiles = profiles;
  }

  @GetMapping("/account")
  public String profile(@AuthenticationPrincipal CurrentUser user, Model model) {
    model.addAttribute(
        "profileForm", ProfileForm.from(user.displayName(), profiles.find(user.id())));
    model.addAttribute(
        "memberSince", users.findById(user.id()).orElseThrow().createdAt().toString());
    return "account/profile";
  }

  @PostMapping("/account")
  public String profile(
      @AuthenticationPrincipal CurrentUser user,
      @Valid @ModelAttribute ProfileForm profileForm,
      BindingResult errors,
      HttpServletResponse response,
      Model model) {
    model.addAttribute(
        "memberSince", users.findById(user.id()).orElseThrow().createdAt().toString());
    if (errors.hasErrors()) return "account/profile";
    profiles.save(user.id(), profileForm);
    for (var preference :
        new String[][] {
          {"civic_language", profileForm.getLanguage()}, {"civic_theme", profileForm.getTheme()}
        })
      response.addHeader(
          "Set-Cookie",
          ResponseCookie.from(preference[0], preference[1])
              .path("/")
              .sameSite("Lax")
              .maxAge(31536000)
              .httpOnly(true)
              .build()
              .toString());
    return "redirect:/account?saved";
  }
}
