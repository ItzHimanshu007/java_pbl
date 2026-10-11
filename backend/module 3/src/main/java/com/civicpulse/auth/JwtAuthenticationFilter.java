package com.civicpulse.auth;

import com.civicpulse.account.UserDao;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtDecoder decoder;
  private final UserDao users;
  private final LoginSessionDao sessions;
  private final AuthCookies cookies;

  public JwtAuthenticationFilter(
      JwtDecoder decoder, UserDao users, LoginSessionDao sessions, AuthCookies cookies) {
    this.decoder = decoder;
    this.users = users;
    this.sessions = sessions;
    this.cookies = cookies;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String token = cookies.read(request);
    if (token != null && !token.isBlank()) {
      try {
        if (token.length() > 4096) throw new IllegalArgumentException("Oversized token");
        var jwt = decoder.decode(token);
        long userId = Long.parseLong(jwt.getSubject());
        String tokenId = jwt.getId();
        var user =
            users
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown account"));
        if (!user.enabled()
            || tokenId == null
            || !sessions.isActive(tokenId, userId, Instant.now())) {
          throw new IllegalArgumentException("Inactive login");
        }
        var principal =
            new CurrentUser(user.id(), user.displayName(), user.email(), user.role(), tokenId);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_" + user.role().name())));
        SecurityContextHolder.setContext(context);
      } catch (JwtException | IllegalArgumentException ex) {
        SecurityContextHolder.clearContext();
        cookies.clear(response);
      }
    }
    chain.doFilter(request, response);
  }
}
