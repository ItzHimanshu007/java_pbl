package com.civicpulse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.account.*;
import com.civicpulse.auth.CurrentUser;
import com.civicpulse.shared.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired UserDao users;
  @Autowired ProfileDao profiles;
  @Autowired JdbcTemplate jdbc;

  @BeforeEach
  void clean() {
    TestDataCleaner.clear(jdbc);
  }

  private AccountUser create(Role role) {
    return users.create("Profile User", role + "@example.com", "unused-test-hash", role);
  }

  private RequestPostProcessor as(AccountUser u) {
    return authentication(
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            .authenticated(
                new CurrentUser(u.id(), u.displayName(), u.email(), u.role(), "test"),
                null,
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(
                    "ROLE_" + u.role())));
  }

  @Test
  void allRolesCanPersistProfileAndHindiDarkPreferences() throws Exception {
    for (var role : Role.values()) {
      var u = create(role);
      mvc.perform(
              post("/account")
                  .with(as(u))
                  .with(csrf())
                  .param("displayName", "Updated Name")
                  .param("dateOfBirth", "2002-05-06")
                  .param("address", "Central Park, Jaipur")
                  .param("phone", "+91 9876543210")
                  .param("city", "Jaipur")
                  .param("postalCode", "302001")
                  .param("neighbourhood", "C Scheme")
                  .param("language", "hi")
                  .param("theme", "dark")
                  .param("homeLatitude", "26.91")
                  .param("homeLongitude", "75.80"))
          .andExpect(redirectedUrl("/account?saved"));
      assertThat(profiles.find(u.id()).language()).isEqualTo("hi");
      assertThat(profiles.find(u.id()).theme()).isEqualTo("dark");
      assertThat(users.findById(u.id()).orElseThrow().displayName()).isEqualTo("Updated Name");
      mvc.perform(get("/account").with(as(u)))
          .andExpect(model().attribute("language", "hi"))
          .andExpect(model().attribute("theme", "dark"));
    }
  }

  @Test
  void malformedPreferencesFutureDobAndPartialLocationDoNotSave() throws Exception {
    var u = create(Role.CITIZEN);
    mvc.perform(
            post("/account")
                .with(as(u))
                .with(csrf())
                .param("displayName", "Valid Name")
                .param("dateOfBirth", "2999-01-01")
                .param("language", "javascript:bad")
                .param("theme", "unknown")
                .param("homeLatitude", "20"))
        .andExpect(model().hasErrors());
    assertThat(profiles.find(u.id()).language()).isEqualTo("en");
    assertThat(users.findById(u.id()).orElseThrow().displayName()).isEqualTo("Profile User");
  }

  @Test
  void postedAccountIdAndRoleCannotChangeAnotherAccount() throws Exception {
    var u = create(Role.CITIZEN);
    var admin = create(Role.ADMIN);
    mvc.perform(
            post("/account")
                .with(as(u))
                .with(csrf())
                .param("displayName", "My name")
                .param("userId", Long.toString(admin.id()))
                .param("role", "ADMIN"))
        .andExpect(redirectedUrl("/account?saved"));
    assertThat(users.findById(u.id()).orElseThrow().role()).isEqualTo(Role.CITIZEN);
    assertThat(users.findById(admin.id()).orElseThrow().displayName()).isEqualTo("Profile User");
  }

  @Test
  void profileSaveRequiresCsrf() throws Exception {
    var u = create(Role.CITIZEN);
    mvc.perform(post("/account").with(as(u)).param("displayName", "Unsaved"))
        .andExpect(status().isForbidden());
    assertThat(users.findById(u.id()).orElseThrow().displayName()).isEqualTo("Profile User");
  }
}
