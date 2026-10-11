package com.civicpulse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.account.UserDao;
import com.civicpulse.auth.AuthCookies;
import com.civicpulse.auth.LoginSessionDao;
import com.civicpulse.shared.Role;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FoundationIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired UserDao users;
  @Autowired PasswordEncoder passwords;
  @Autowired JwtEncoder encoder;
  @Autowired JwtDecoder decoder;
  @Autowired LoginSessionDao sessions;
  private final String password = "AnActualPassword!2026";

  @BeforeEach
  void resetAccounts() {
    TestDataCleaner.clear(jdbc);
  }

  private Cookie login(Role role) throws Exception {
    String email = role.name().toLowerCase() + "@example.com";
    users.create("Test " + role.label(), email, passwords.encode(password), role);
    MvcResult result =
        mvc.perform(post("/login").with(csrf()).param("email", email).param("password", password))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(role.dashboard()))
            .andReturn();
    String header =
        result.getResponse().getHeaders("Set-Cookie").stream()
            .filter(value -> value.startsWith(AuthCookies.NAME + "="))
            .findFirst()
            .orElseThrow();
    String token = header.substring((AuthCookies.NAME + "=").length(), header.indexOf(';'));
    assertThat(header).contains("HttpOnly", "SameSite=Lax", "Max-Age=1800");
    assertThat(decoder.decode(token).getSubject()).isNotBlank();
    return new Cookie(AuthCookies.NAME, token);
  }

  @Test
  void publicPagesAreAccessible() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
    mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
    mvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(view().name("auth/register"));
  }

  @Test
  void registrationHashesPasswordAndNeverAcceptsPrivilegedRole() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("displayName", "Asha")
                .param("email", "ASHA@example.com")
                .param("password", password)
                .param("confirmPassword", password)
                .param("role", "ADMIN")
                .param("enabled", "false"))
        .andExpect(redirectedUrl("/login"));
    var user = users.findByEmail("asha@example.com").orElseThrow();
    assertThat(user.role()).isEqualTo(Role.CITIZEN);
    assertThat(user.enabled()).isTrue();
    assertThat(user.passwordHash()).isNotEqualTo(password).startsWith("$2");
    assertThat(passwords.matches(password, user.passwordHash())).isTrue();
  }

  @Test
  void registrationRejectsDuplicateNormalizedEmail() throws Exception {
    users.create("Existing", "asha@example.com", passwords.encode(password), Role.CITIZEN);
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("displayName", "Another")
                .param("email", "ASHA@example.com")
                .param("password", password)
                .param("confirmPassword", password))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("registrationForm", "email"));
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class)).isEqualTo(1);
  }

  @Test
  void registrationRejectsMismatchedPasswords() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("displayName", "Asha")
                .param("email", "asha@example.com")
                .param("password", password)
                .param("confirmPassword", "DifferentPassword!"))
        .andExpect(status().isOk())
        .andExpect(model().hasErrors());
    assertThat(users.findByEmail("asha@example.com")).isEmpty();
  }

  @Test
  void registrationRejectsInvalidEmailAndShortPassword() throws Exception {
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("displayName", "Asha")
                .param("email", "not-email")
                .param("password", "short")
                .param("confirmPassword", "short"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("registrationForm", "email", "password"));
  }

  @Test
  void registrationRejectsMultibytePasswordBeyondBcryptLimit() throws Exception {
    String longPassword = "界".repeat(30);
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("displayName", "Asha")
                .param("email", "asha@example.com")
                .param("password", longPassword)
                .param("confirmPassword", longPassword))
        .andExpect(status().isOk())
        .andExpect(model().hasErrors());
    assertThat(users.findByEmail("asha@example.com")).isEmpty();
  }

  @Test
  void loginRejectsIncorrectCredentials() throws Exception {
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("email", "missing@example.com")
                .param("password", password))
        .andExpect(status().isOk())
        .andExpect(model().hasErrors());
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM login_sessions", Integer.class)).isZero();
  }

  @Test
  void citizenLoginRoutesToCitizenWorkspace() throws Exception {
    Cookie cookie = login(Role.CITIZEN);
    mvc.perform(get("/citizen/dashboard").cookie(cookie))
        .andExpect(
            result -> {
              assertThat(result.getResponse().getStatus()).isIn(200, 302);
              if (result.getResponse().getStatus() == 302)
                assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/feed");
            });
    mvc.perform(get("/dashboard").cookie(cookie)).andExpect(redirectedUrl("/citizen/dashboard"));
  }

  @Test
  void officerLoginRoutesToOfficerWorkspace() throws Exception {
    mvc.perform(get("/officer/dashboard").cookie(login(Role.OFFICER))).andExpect(status().isOk());
  }

  @Test
  void adminLoginRoutesToAdminWorkspace() throws Exception {
    mvc.perform(get("/admin/dashboard").cookie(login(Role.ADMIN))).andExpect(status().isOk());
  }

  @Test
  void citizenCannotAccessOtherRoleRoutes() throws Exception {
    Cookie cookie = login(Role.CITIZEN);
    mvc.perform(get("/admin/dashboard").cookie(cookie)).andExpect(status().isForbidden());
    mvc.perform(get("/officer/dashboard").cookie(cookie)).andExpect(status().isForbidden());
  }

  @Test
  void unauthenticatedUserIsRedirectedToLogin() throws Exception {
    mvc.perform(get("/account")).andExpect(redirectedUrl("/login?expired"));
  }

  @Test
  void missingCsrfIsRejectedEvenWithValidJwt() throws Exception {
    Cookie cookie = login(Role.CITIZEN);
    mvc.perform(post("/account").cookie(cookie).param("displayName", "Unsafe update"))
        .andExpect(status().isForbidden());
  }

  @Test
  void logoutRevokesTokenIncludingReplayOfOriginalCookie() throws Exception {
    Cookie original = login(Role.CITIZEN);
    mvc.perform(post("/logout").with(csrf()).cookie(original))
        .andExpect(redirectedUrl("/login?loggedOut"));
    mvc.perform(get("/citizen/dashboard").cookie(original))
        .andExpect(redirectedUrl("/login?expired"));
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM login_sessions WHERE revoked = TRUE", Integer.class))
        .isEqualTo(1);
  }

  @Test
  void tamperedJwtIsRejected() throws Exception {
    Cookie valid = login(Role.CITIZEN);
    String token = valid.getValue();
    int signatureStart = token.lastIndexOf('.') + 1;
    char replacement = token.charAt(signatureStart) == 'a' ? 'b' : 'a';
    Cookie tampered =
        new Cookie(
            AuthCookies.NAME,
            token.substring(0, signatureStart) + replacement + token.substring(signatureStart + 1));
    mvc.perform(get("/citizen/dashboard").cookie(tampered))
        .andExpect(redirectedUrl("/login?expired"));
  }

  @Test
  void expiredSignedTokenIsRejected() throws Exception {
    var user =
        users.create("Expired", "expired@example.com", passwords.encode(password), Role.CITIZEN);
    String id = UUID.randomUUID().toString();
    Instant expiry = Instant.now().minusSeconds(120);
    sessions.create(id, user.id(), expiry);
    var claims =
        JwtClaimsSet.builder()
            .issuer("civicpulse")
            .subject(Long.toString(user.id()))
            .id(id)
            .issuedAt(expiry.minusSeconds(1800))
            .expiresAt(expiry)
            .claim("role", "CITIZEN")
            .build();
    String token =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    mvc.perform(get("/account").cookie(new Cookie(AuthCookies.NAME, token)))
        .andExpect(redirectedUrl("/login?expired"));
  }

  @Test
  void disabledUserCannotReuseExistingToken() throws Exception {
    Cookie token = login(Role.CITIZEN);
    jdbc.update("UPDATE users SET enabled = FALSE WHERE email = ?", "citizen@example.com");
    mvc.perform(get("/account").cookie(token)).andExpect(redirectedUrl("/login?expired"));
  }

  @Test
  void profileUpdateCannotChangeRoleOrOtherUsers() throws Exception {
    var other =
        users.create("Other", "other@example.com", passwords.encode(password), Role.CITIZEN);
    Cookie token = login(Role.CITIZEN);
    mvc.perform(
            post("/account")
                .with(csrf())
                .cookie(token)
                .param("displayName", "Updated Name")
                .param("id", Long.toString(other.id()))
                .param("role", "ADMIN"))
        .andExpect(redirectedUrl("/account?saved"));
    var own = users.findByEmail("citizen@example.com").orElseThrow();
    assertThat(own.displayName()).isEqualTo("Updated Name");
    assertThat(own.role()).isEqualTo(Role.CITIZEN);
    assertThat(users.findById(other.id()).orElseThrow().displayName()).isEqualTo("Other");
  }

  // csrf() installs a mock repository in the shared filter. Use a fresh context for a real cookie
  // round trip.
  @org.springframework.test.annotation.DirtiesContext(
      methodMode = org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD)
  @Test
  void realCsrfCookieRoundTripAllowsLoginAndAuthenticatedForm() throws Exception {
    users.create("Cookie Flow", "cookie@example.com", passwords.encode(password), Role.CITIZEN);
    MvcResult loginPage = mvc.perform(get("/login")).andReturn();
    var csrfToken =
        (org.springframework.security.web.csrf.CsrfToken)
            loginPage.getRequest().getAttribute("_csrf");
    String value = csrfToken.getToken();
    Cookie csrfCookie = new Cookie("civic_csrf", value);
    MvcResult loginResult =
        mvc.perform(
                post("/login")
                    .cookie(csrfCookie)
                    .param("_csrf", value)
                    .param("email", "cookie@example.com")
                    .param("password", password))
            .andExpect(redirectedUrl("/citizen/dashboard"))
            .andReturn();
    String authHeader =
        loginResult.getResponse().getHeaders("Set-Cookie").stream()
            .filter(header -> header.startsWith(AuthCookies.NAME + "="))
            .findFirst()
            .orElseThrow();
    Cookie jwt =
        new Cookie(
            AuthCookies.NAME,
            authHeader.substring((AuthCookies.NAME + "=").length(), authHeader.indexOf(';')));
    MvcResult accountPage =
        mvc.perform(get("/account").cookie(csrfCookie, jwt)).andExpect(status().isOk()).andReturn();
    String accountToken =
        ((org.springframework.security.web.csrf.CsrfToken)
                accountPage.getRequest().getAttribute("_csrf"))
            .getToken();
    // Apply Set-Cookie updates as a real browser does when a deferred token is refreshed.
    String csrfHeader =
        accountPage.getResponse().getHeaders("Set-Cookie").stream()
            .filter(header -> header.startsWith("civic_csrf="))
            .reduce((first, last) -> last)
            .orElse(null);
    if (csrfHeader != null) {
      csrfCookie =
          new Cookie(
              "civic_csrf", csrfHeader.substring("civic_csrf=".length(), csrfHeader.indexOf(';')));
    }
    assertThat(csrfCookie.getValue()).isEqualTo(accountToken);
    // Browsers fetch assets after the HTML. Those requests must not invalidate the form token.
    MvcResult asset =
        mvc.perform(get("/assets/brand.svg").cookie(csrfCookie, jwt))
            .andExpect(status().isOk())
            .andReturn();
    assertThat(asset.getResponse().getHeaders("Set-Cookie"))
        .noneMatch(header -> header.startsWith("civic_csrf="));
    mvc.perform(
            post("/account")
                .cookie(csrfCookie, jwt)
                .param("_csrf", accountToken)
                .param("displayName", "Saved through real CSRF"))
        .andExpect(redirectedUrl("/account?saved"));
    assertThat(users.findByEmail("cookie@example.com").orElseThrow().displayName())
        .isEqualTo("Saved through real CSRF");
  }
}
