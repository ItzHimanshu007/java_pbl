package com.civicpulse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.account.ProfileDao;
import com.civicpulse.insights.InsightsService;
import com.civicpulse.location.JaipurPlaces;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JaipurServicesIntegrationTest extends FeatureTestSupport {
  @Autowired JaipurPlaces places;
  @Autowired InsightsService insights;
  @Autowired ProfileDao profiles;

  @Test
  void serviceCatalogueRoutesEveryCategoryWithATargetTime() {
    var categories = dao.categories();
    assertThat(categories).hasSizeGreaterThanOrEqualTo(16);
    assertThat(categories.get(0).name()).isEqualTo("Roads");
    assertThat(categories.get(0).getCode()).isEqualTo("road");
    assertThat(categories).allSatisfy(c -> assertThat(c.slaHours()).isPositive());
    assertThat(categories.stream().map(c -> c.departmentId()).distinct().count())
        .isGreaterThanOrEqualTo(9);
  }

  @Test
  void jaipurLocalitiesMatchInEnglishAndHindiAndResolveZones() {
    assertThat(places.search("hawa", "en", 5).get(0).label()).startsWith("Hawa Mahal");
    assertThat(places.search("c scheme", "en", 5).get(0).label()).startsWith("C-Scheme");
    assertThat(places.search("आमेर", "hi", 5).get(0).label()).startsWith("आमेर");
    assertThat(places.zoneOf(26.9239, 75.8267)).isEqualTo("Walled City");
    assertThat(places.zoneOf(26.8550, 75.8240)).isEqualTo("Malviya Nagar");
    assertThat(places.zoneOf(28.6139, 77.2090)).isEqualTo(JaipurPlaces.OTHER_AREAS);
    assertThat(places.zones()).hasSize(12).allSatisfy(z -> assertThat(z.places()).isNotEmpty());
  }

  @Test
  void addressSearchFallsBackToLocalitiesWhenTheGeocoderIsDown() throws Exception {
    mvc.perform(get("/locations/search").param("q", "Johari").with(as(citizen)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].label").value("Johari Bazaar, Walled City, Jaipur"));
    mvc.perform(get("/locations/search").param("q", "जौहरी").param("lang", "hi").with(as(citizen)))
        .andExpect(jsonPath("$[0].label").value("जौहरी बाज़ार, परकोटा, जयपुर"));
    mvc.perform(
            get("/locations/reverse")
                .param("latitude", "26.9240")
                .param("longitude", "75.8268")
                .with(as(citizen)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].label").value("Near Hawa Mahal, Walled City, Jaipur"))
        .andExpect(jsonPath("$[0].latitude").value(26.924));
    // No local match and no geocoder: unavailable (503) or throttled (429).
    int unavailable =
        mvc.perform(get("/locations/search").param("q", "zzqxw").with(as(citizen)))
            .andReturn()
            .getResponse()
            .getStatus();
    assertThat(unavailable).isIn(429, 503);
  }

  @Test
  void publicTrackingShowsProgressWithoutPrivateDetails() throws Exception {
    long id = report();
    var c = dao.find(id);
    var model =
        mvc.perform(get("/track").param("ref", c.reference().toLowerCase()))
            .andExpect(status().isOk())
            .andExpect(view().name("track"))
            .andExpect(model().attributeExists("report", "updates", "zone"))
            .andReturn()
            .getModelAndView()
            .getModel();
    assertThat(((com.civicpulse.complaints.Complaint) model.get("report")).id()).isEqualTo(id);
    assertThat(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(model.get("updates")))
        .doesNotContain("actor", "note", citizen.email());
    mvc.perform(get("/track").param("ref", "CP-2026-NOTAREALREF"))
        .andExpect(model().attribute("notFound", true));
    submission.delete(id, citizen);
    mvc.perform(get("/track").param("ref", c.reference()))
        .andExpect(model().attribute("notFound", true));
  }

  @Test
  void languageSwitchWorksBeforeAndAfterSignInAndRejectsOffsiteRedirects() throws Exception {
    mvc.perform(post("/preferences").with(csrf()).param("language", "hi").param("next", "/track"))
        .andExpect(redirectedUrl("/track"))
        .andExpect(cookie().value("civic_language", "hi"));
    mvc.perform(
            post("/preferences")
                .with(csrf())
                .with(as(citizen))
                .param("language", "hi")
                .param("theme", "dark")
                .param("next", "//evil.example/steal"))
        .andExpect(redirectedUrl("/"));
    assertThat(profiles.find(citizen.id()).language()).isEqualTo("hi");
    assertThat(profiles.find(citizen.id()).theme()).isEqualTo("dark");
    mvc.perform(post("/preferences").param("language", "hi")).andExpect(status().isForbidden());
  }

  @Test
  void browserDictionaryIsServedFromTheSameTranslations() throws Exception {
    var script =
        mvc.perform(get("/i18n/messages.js"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    assertThat(script).contains("window.CivicHindi=", "window.CivicEnglish=", "सड़कें");
  }

  @Test
  void homePageShowsCityWideFiguresWithoutSignIn() throws Exception {
    report();
    mvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("publicStats", "serviceCategories", "zones"));
    assertThat(insights.publicStats().total()).isEqualTo(1);
  }

  @Test
  void reportsPastTheirTargetTimeAreOverdue() throws Exception {
    long id = report();
    assertThat(dao.find(id).isOverdue()).isFalse();
    jdbc.update(
        "UPDATE complaints SET created_at=? WHERE id=?",
        Timestamp.from(Instant.now().minus(Duration.ofDays(8))),
        id);
    assertThat(dao.find(id).isOverdue()).isTrue();
    assertThat(insights.overdue(admin, 5)).extracting(c -> c.id()).containsExactly(id);
    assertThat(insights.overdueCount(citizen)).isEqualTo(1);
    jdbc.update("UPDATE complaints SET status='CLOSED' WHERE id=?", id);
    assertThat(insights.overdueCount(admin)).isZero();
  }

  @Test
  void zoneAndCategoryChartsFollowRoleScope() throws Exception {
    report();
    var hawaMahal = form();
    hawaMahal.setLatitude(26.9239);
    hawaMahal.setLongitude(75.8267);
    submission.submit(hawaMahal, photo(), other);
    assertThat(insights.zoneRows(admin)).extracting(r -> r.label()).contains("Walled City");
    assertThat(insights.zoneRows(citizen)).extracting(r -> r.label()).doesNotContain("Walled City");
    assertThat(insights.categoryRows(admin).get(0).value()).isEqualTo(2);
    assertThat(insights.categoryRows(admin).get(0).percent()).isEqualTo(100);
  }
}
