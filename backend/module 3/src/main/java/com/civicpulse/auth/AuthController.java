package com.civicpulse.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
  private final AuthService auth;
  private final AuthCookies cookies;
  private final LoginSessionDao sessions;
  private final CookieCsrfTokenRepository csrf;

  public AuthController(
      AuthService auth,
      AuthCookies cookies,
      LoginSessionDao sessions,
      CookieCsrfTokenRepository csrf) {
    this.auth = auth;
    this.cookies = cookies;
    this.sessions = sessions;
    this.csrf = csrf;
  }

  @GetMapping("/login")
  public String login(
      @AuthenticationPrincipal CurrentUser user, Model model, HttpServletRequest request) {
    if (user != null) return "redirect:" + user.role().dashboard();
    model.addAttribute("loginForm", new LoginForm());
    var session = request.getSession(false);
    if (session != null && Boolean.TRUE.equals(session.getAttribute("registrationCompleted"))) {
      model.addAttribute("registrationCompleted", true);
      model.addAttribute("locationWelcome", true);
      session.removeAttribute("registrationCompleted");
      session.invalidate();
    }
    return "auth/login";
  }

  @PostMapping("/login")
  public String login(
      @Valid @ModelAttribute LoginForm loginForm,
      BindingResult errors,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (errors.hasErrors()) return "auth/login";
    try {
      var user = auth.authenticate(loginForm);
      cookies.set(response, auth.issueLogin(user));
      csrf.saveToken(null, request, response);
      var session = request.getSession(false);
      if (session != null) session.invalidate();
      return "redirect:" + user.role().dashboard();
    } catch (IllegalArgumentException ex) {
      errors.reject("invalidLogin", "Email or password is incorrect.");
      return "auth/login";
    }
  }

  @GetMapping("/register")
  public String register(@AuthenticationPrincipal CurrentUser user, Model model) {
    if (user != null) return "redirect:" + user.role().dashboard();
    model.addAttribute("registrationForm", new RegistrationForm());
    return "auth/register";
  }

  @PostMapping("/register")
  public String register(
      @Valid @ModelAttribute RegistrationForm registrationForm,
      BindingResult errors,
      HttpServletRequest request) {
    if (errors.hasErrors()) return "auth/register";
    try {
      auth.register(registrationForm);
    } catch (DuplicateKeyException ex) {
      errors.rejectValue("email", "duplicateEmail", "This email is already registered.");
      return "auth/register";
    } catch (IllegalArgumentException ex) {
      errors.reject("invalidRegistration", ex.getMessage());
      return "auth/register";
    }
    // This short-lived UI session carries a confirmation only; authentication remains JWT-based.
    request.getSession(true).setAttribute("registrationCompleted", true);
    return "redirect:/login";
  }

  @PostMapping("/logout")
  public String logout(
      @AuthenticationPrincipal CurrentUser user,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (user != null) sessions.revoke(user.tokenId());
    cookies.clear(response);
    csrf.saveToken(null, request, response);
    var session = request.getSession(false);
    if (session != null) session.invalidate();
    return "redirect:/login?loggedOut";
  }
}
