package com.civicpulse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.insights.*;
import com.civicpulse.shared.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CitizenExperienceIntegrationTest extends FeatureTestSupport {
  @Autowired InsightsService insights;
  @Autowired com.civicpulse.resolution.ResolutionService resolution;

  @Test
  void ownerDeletionHidesReportEvidenceFeedAndStatsAndCanRestore() throws Exception {
    long id = report(), image = images.list(id).get(0).id();
    mvc.perform(post("/citizen/reports/" + id + "/delete").with(as(other)).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(post("/citizen/reports/" + id + "/delete").with(as(citizen)))
        .andExpect(status().isForbidden());
    mvc.perform(post("/citizen/reports/" + id + "/delete").with(as(citizen)).with(csrf()))
        .andExpect(redirectedUrl("/citizen/reports?deleted"));
    assertThat(dao.count("all", null, null, null)).isZero();
    assertThat(insights.community(other)).isEmpty();
    assertThat(dao.list("trash", citizen.id(), null, null, 0, 10)).hasSize(1);
    assertThat(insights.nearby(1, 26.9124, 75.7873, other)).isEmpty();
    mvc.perform(get("/reports/images/" + image).with(as(citizen))).andExpect(status().isNotFound());
    mvc.perform(post("/citizen/reports/" + id + "/restore").with(as(other)).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(post("/citizen/reports/" + id + "/restore").with(as(citizen)).with(csrf()))
        .andExpect(redirectedUrl("/citizen/reports?restored"));
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.REPORTED);
    assertThat(dao.events(id)).hasSize(3);
  }

  @Test
  void deletedAssignedReportCannotReceiveOfficerUpdates() throws Exception {
    long id = report();
    jdbc.update(
        "UPDATE complaints SET status='ASSIGNED',assigned_officer_id=? WHERE id=?",
        officer.id(),
        id);
    submission.delete(id, citizen);
    assertThatThrownBy(() -> resolution.start(id, "Inspect the road", officer))
        .isInstanceOf(FeatureException.class);
    submission.restore(id, citizen);
    resolution.start(id, "Inspect the road", officer);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.IN_PROGRESS);
  }

  @Test
  void linkedReportsCountDistinctCitizensAndAppearOnce() throws Exception {
    long root = report();
    var linked = form();
    linked.setRelatedReportId(root);
    submission.submit(linked, photo(), other);
    submission.submit(linked, photo(), other);
    var feed = insights.feed(admin, 26.9124, 75.7873, 5, false);
    assertThat(feed).hasSize(1);
    assertThat(feed.get(0).repeatCount()).isEqualTo(2);
    assertThat(feed.get(0).priority()).isEqualTo("medium");
    for (int i = 0; i < 3; i++)
      submission.submit(linked, photo(), account("repeat" + i, Role.CITIZEN));
    assertThat(insights.feed(admin, null, null, 5, false).get(0).priority()).isEqualTo("high");
  }

  @Test
  void nearbyReportsDoNotAutomaticallyClaimToBeTheSameIssue() throws Exception {
    report();
    submission.submit(form(), photo(), other);
    assertThat(insights.feed(admin, null, null, 5, false))
        .hasSize(2)
        .allSatisfy(
            c -> {
              assertThat(c.repeatCount()).isEqualTo(1);
              assertThat(c.priority()).isEqualTo("low");
            });
  }

  @Test
  void farOrDifferentCategoryIssueLinksAreRejectedWithoutWriting() throws Exception {
    long root = report();
    var linked = form();
    linked.setRelatedReportId(root);
    linked.setLatitude(27.5);
    assertThatThrownBy(() -> submission.submit(linked, photo(), other))
        .isInstanceOf(FeatureException.class);
    linked.setLatitude(26.9124);
    linked.setCategoryId(2L);
    assertThatThrownBy(() -> submission.submit(linked, photo(), other))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.count("all", null, null, null)).isEqualTo(1);
  }

  @Test
  void feedFiltersAreaAndOwnReportsAndPublicTimelineProtectsPrivateData() throws Exception {
    long own = report(), near = submission.submit(form(), photo(), other);
    var far = form();
    far.setLatitude(28.0);
    submission.submit(far, photo(), other);
    var rows = insights.feed(citizen, 26.9124, 75.7873, 2, true);
    assertThat(rows).hasSize(1);
    assertThat(rows.get(0).id()).isEqualTo(near);
    mvc.perform(get("/community/" + near).with(as(citizen))).andExpect(status().isOk());
    assertThat(
            new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(insights.publicUpdates(own)))
        .doesNotContain("actor", "email", "phone", "note", "dateOfBirth");
    submission.delete(near, other);
    mvc.perform(get("/community/" + near).with(as(citizen))).andExpect(status().isNotFound());
  }
}
