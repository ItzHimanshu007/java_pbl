package com.civicpulse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.complaints.*;
import com.civicpulse.insights.InsightsService;
import com.civicpulse.resolution.ResolutionService;
import com.civicpulse.shared.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnonymousReportingIntegrationTest extends FeatureTestSupport {
  @Autowired ReporterIdentity identity;
  @Autowired ComplaintAccess access;
  @Autowired ResolutionService resolution;
  @Autowired InsightsService insights;

  private long anonymousReport() throws Exception {
    var f = form();
    f.setAnonymous(true);
    return submission.submit(f, photo(), citizen);
  }

  @Test
  void anonymousReportStoresOnlyAKeyedHashOfTheReporter() throws Exception {
    long id = anonymousReport();
    var c = dao.find(id);
    assertThat(c.anonymous()).isTrue();
    assertThat(c.reporterId()).isNotEqualTo(citizen.id());
    var filedUnder = users.findById(c.reporterId()).orElseThrow();
    assertThat(filedUnder.email()).isEqualTo(ReporterIdentity.ANONYMOUS_EMAIL);
    assertThat(filedUnder.enabled()).isFalse();
    assertThat(c.reporterHash())
        .matches("[0-9a-f]{64}")
        .isEqualTo(identity.hash(citizen.id()))
        .doesNotContain(citizen.email(), citizen.displayName());
    // Nothing in the report, its photo or its timeline points at the citizen's account.
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE id=? AND reporter_id=?",
                Long.class,
                id,
                citizen.id()))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM attachments WHERE complaint_id=? AND uploader_id=?",
                Long.class,
                id,
                citizen.id()))
        .isZero();
    assertThat(dao.events(id)).extracting(ComplaintEvent::getActor).containsOnly("Anonymous citizen");
  }

  @Test
  void hashIsStablePerAccountAndDependsOnTheServerKey() {
    assertThat(identity.hash(citizen.id())).isEqualTo(identity.hash(citizen.id()));
    assertThat(identity.hash(citizen.id())).isNotEqualTo(identity.hash(other.id()));
    var otherKey =
        new ReporterIdentity(Base64.getEncoder().encodeToString(new byte[32]), null, users);
    assertThat(otherKey.hash(citizen.id())).isNotEqualTo(identity.hash(citizen.id()));
    assertThatThrownBy(() -> new ReporterIdentity("c2hvcnQ=", null, users))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void onlyTheRealReporterCanSeeDeleteAndRestoreAnAnonymousReport() throws Exception {
    long id = anonymousReport();
    assertThat(dao.list("mine", citizen.id(), null, null, 0, 10)).extracting(Complaint::id).contains(id);
    assertThat(dao.list("mine", other.id(), null, null, 0, 10)).isEmpty();
    assertThat(access.isOwner(dao.find(id), citizen)).isTrue();
    assertThat(access.isOwner(dao.find(id), other)).isFalse();
    mvc.perform(get("/reports/" + id).with(as(citizen))).andExpect(status().isOk());
    mvc.perform(get("/reports/" + id).with(as(other))).andExpect(status().isForbidden());
    mvc.perform(get("/reports/" + id).with(as(admin)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("canDelete", false));
    mvc.perform(post("/citizen/reports/" + id + "/delete").with(as(other)).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(post("/citizen/reports/" + id + "/delete").with(as(citizen)).with(csrf()))
        .andExpect(redirectedUrl("/citizen/reports?deleted"));
    assertThat(dao.list("trash", citizen.id(), null, null, 0, 10)).hasSize(1);
    submission.restore(id, citizen);
    assertThat(dao.events(id)).extracting(ComplaintEvent::getActor).containsOnly("Anonymous citizen");
    assertThat(insights.totals(citizen).get("REPORTED")).isEqualTo(1);
  }

  @Test
  void anonymousReporterVerifiesTheRepairWithoutRevealingThemselves() throws Exception {
    long id = anonymousReport();
    jdbc.update(
        "UPDATE complaints SET status='ASSIGNED',assigned_officer_id=? WHERE id=?",
        officer.id(),
        id);
    resolution.start(id, "Repair crew starts work", officer);
    resolution.resolve(id, "Surface repaired and inspected", photo(), officer);
    mvc.perform(get("/reports/" + id).with(as(citizen))).andExpect(model().attribute("canVerify", true));
    mvc.perform(get("/reports/" + id).with(as(other))).andExpect(status().isForbidden());
    assertThatThrownBy(() -> resolution.confirm(id, other)).isInstanceOf(FeatureException.class);
    resolution.confirm(id, citizen);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.CLOSED);
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM complaint_events WHERE complaint_id=? AND actor_id=?",
                Long.class,
                id,
                citizen.id()))
        .isZero();
  }

  @Test
  void communityTreatsAnonymousReportsAsTheReportersOwnAndCountsEachCitizenOnce()
      throws Exception {
    long root = report();
    var linked = form();
    linked.setAnonymous(true);
    linked.setRelatedReportId(root);
    // The same citizen reporting again anonymously still counts once.
    submission.submit(linked, photo(), citizen);
    assertThat(insights.feed(admin, null, null, 5, false).get(0).repeatCount()).isEqualTo(1);
    long anonymous = anonymousReport();
    assertThat(insights.community(citizen))
        .filteredOn(r -> r.id() == anonymous)
        .singleElement()
        .satisfies(r -> assertThat(r.mine()).isTrue());
    assertThat(insights.community(other))
        .filteredOn(r -> r.id() == anonymous)
        .singleElement()
        .satisfies(r -> assertThat(r.mine()).isFalse());
    assertThatThrownBy(() -> insights.support(anonymous, citizen))
        .isInstanceOf(FeatureException.class);
    insights.support(anonymous, other);
  }

  @Test
  void anonymousCheckboxIsBoundFromTheReportForm() throws Exception {
    mvc.perform(
            multipart("/citizen/reports")
                .file(photo())
                .with(as(citizen))
                .with(csrf())
                .param("title", form().getTitle())
                .param("description", form().getDescription())
                .param("address", form().getAddress())
                .param("categoryId", "1")
                .param("latitude", "26.9")
                .param("longitude", "75.8")
                .param("anonymous", "true"))
        .andExpect(status().is3xxRedirection());
    var c = dao.list("mine", citizen.id(), null, null, 0, 10).get(0);
    assertThat(c.anonymous()).isTrue();
    assertThat(c.reporterId()).isNotEqualTo(citizen.id());
  }
}
