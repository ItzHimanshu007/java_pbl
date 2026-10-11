package com.civicpulse;

import static org.assertj.core.api.Assertions.*;

import com.civicpulse.insights.*;
import com.civicpulse.shared.*;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsightsIntegrationTest extends FeatureTestSupport {
  @Autowired InsightsService insights;

  @Test
  void nearbyUsesCategoryDistanceAndActiveStatus() throws Exception {
    long near = report();
    var far = form();
    far.setLatitude(26.94);
    submission.submit(far, photo(), citizen);
    var otherCategory = form();
    otherCategory.setCategoryId(2L);
    submission.submit(otherCategory, photo(), citizen);
    var result = insights.nearby(1, 26.9124, 75.7873, other);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(near);
    jdbc.update("UPDATE complaints SET status='CLOSED' WHERE id=?", near);
    assertThat(insights.nearby(1, 26.9124, 75.7873, other)).isEmpty();
  }

  @Test
  void haversineHandlesIdenticalAndDatelineCoordinates() {
    assertThat(InsightsService.distance(0, 0, 0, 0)).isZero();
    assertThat(InsightsService.distance(0, 179.999, 0, -179.999)).isBetween(220.0, 225.0);
  }

  @Test
  void nearbyFindsReportsAcrossDatelineAndRejectsNan() throws Exception {
    var f = form();
    f.setLatitude(0.0);
    f.setLongitude(-179.999);
    submission.submit(f, photo(), citizen);
    assertThat(insights.nearby(1, 0, 179.999, other)).hasSize(1);
    assertThatThrownBy(() -> insights.nearby(1, Double.NaN, 0, other))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void supportIsUniqueAndOwnOrInactiveReportIsRejected() throws Exception {
    long id = report();
    insights.support(id, other);
    insights.support(id, other);
    assertThat(insights.community(other).get(0).supports()).isEqualTo(1);
    assertThat(insights.community(other).get(0).supported()).isTrue();
    assertThatThrownBy(() -> insights.support(id, citizen)).isInstanceOf(FeatureException.class);
    assertThatThrownBy(() -> insights.support(id, officer)).isInstanceOf(FeatureException.class);
    jdbc.update("UPDATE complaints SET status='CLOSED' WHERE id=?", id);
    assertThatThrownBy(() -> insights.support(id, other)).isInstanceOf(FeatureException.class);
  }

  @Test
  void communityDtoDoesNotExposeReporterOrEvidenceFields() throws Exception {
    report();
    String json =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .writeValueAsString(insights.community(other));
    assertThat(json)
        .doesNotContain(
            "reporterId", "email", "description", "image_data", "password", "officerId");
  }

  @Test
  void metricsAndCsvFollowRoleScope() throws Exception {
    long id = report();
    submission.submit(form(), photo(), other);
    assertThat(insights.totals(citizen).get("REPORTED")).isEqualTo(1);
    assertThat(insights.totals(admin).get("REPORTED")).isEqualTo(2);
    assertThat(insights.csv(citizen, "", ""))
        .contains(dao.find(id).reference())
        .doesNotContain("citizen@test.example");
    assertThat(insights.csv(officer, "", "").split("\r\n")).hasSize(1);
    assertThat(insights.categories(citizen).get("Roads")).isEqualTo(1);
  }

  @Test
  void closedTimeUsesOnlyVerifiedClosures() throws Exception {
    assertThat(insights.averageHours(admin)).isEqualTo("—");
    long id = report();
    Instant now = Instant.now();
    jdbc.update(
        "UPDATE complaints SET status='CLOSED',created_at=?,closed_at=? WHERE id=?",
        Timestamp.from(now.minusSeconds(7200)),
        Timestamp.from(now),
        id);
    assertThat(insights.averageHours(citizen)).isEqualTo("2.0");
    assertThat(insights.averageHours(other)).isEqualTo("—");
  }

  @Test
  void csvCellsEscapeQuotesAndFormulaPrefixes() {
    assertThat(InsightsService.cell("=1+1")).isEqualTo("\"'=1+1\"");
    assertThat(InsightsService.cell("  @SUM(1,2)")).startsWith("\"'");
    assertThat(InsightsService.cell("a\"b")).isEqualTo("\"a\"\"b\"");
  }
}
