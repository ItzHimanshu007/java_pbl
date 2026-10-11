package com.civicpulse.auth;

import com.civicpulse.config.JwtSettings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookies {
  public static final String NAME = "civic_token";
  private final JwtSettings settings;

  public AuthCookies(JwtSettings settings) {
    this.settings = settings;
  }

  public void set(HttpServletResponse response, String token) {
    write(response, token, settings.tokenLifetime());
  }

  public void clear(HttpServletResponse response) {
    write(response, "", Duration.ZERO);
  }

  private void write(HttpServletResponse response, String value, Duration age) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        ResponseCookie.from(NAME, value)
            .httpOnly(true)
            .secure(settings.secureCookie())
            .sameSite("Lax")
            .path("/")
            .maxAge(age)
            .build()
            .toString());
  }

  public String read(HttpServletRequest request) {
    if (request.getCookies() == null) return null;
    for (var cookie : request.getCookies())
      if (NAME.equals(cookie.getName())) return cookie.getValue();
    return null;
  }
}
