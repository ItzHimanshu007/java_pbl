package com.civicpulse;

import static org.assertj.core.api.Assertions.*;

import com.civicpulse.complaints.ReporterIdentity;
import com.civicpulse.insights.DemoReports;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Sample reports added once to a real database for a public demonstration (CP_SAMPLE_REPORTS). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SampleReportsIntegrationTest extends FeatureTestSupport {
  @Autowired DemoReports samples;
  @Autowired ReporterIdentity identity;

  private long count(String sql, Object... args) {
    return jdbc.queryForObject(sql, Long.class, args);
  }

  private long sampleReports() {
    return count("SELECT COUNT(*) FROM complaints WHERE reference LIKE 'CP-DEMO-%'");
  }

  @Test
  void sampleReportsUseTheGivenAccountsAndAreAddedOnlyOnce() {
    assertThat(samples.addSampleReports(" Citizen@Test.Example ", "officer@test.example", ""))
        .isTrue();
    long added = sampleReports();
    assertThat(added).isGreaterThan(40);
    // The demo's own reports belong to the given citizen account.
    assertThat(dao.list("mine", citizen.id(), null, null, 0, 50)).hasSizeGreaterThanOrEqualTo(5);
    // The given officer takes their department's work, and nobody works outside their department.
    assertThat(count("SELECT COUNT(*) FROM complaints WHERE assigned_officer_id=?", officer.id()))
        .isPositive();
    assertThat(
            count(
                "SELECT COUNT(*) FROM complaints c JOIN users u ON u.id=c.assigned_officer_id "
                    + "WHERE u.department_id<>c.department_id"))
        .isZero();
    // Assignments are recorded by the existing administrator.
    assertThat(
            count(
                "SELECT COUNT(*) FROM complaint_events WHERE action='ASSIGNED' AND actor_id<>?",
                admin.id()))
        .isZero();
    // Anonymous samples are filed under the anonymous account with only the reporter hash.
    assertThat(
            count(
                "SELECT COUNT(*) FROM complaints WHERE is_anonymous=TRUE AND reporter_id=?",
                identity.anonymousAccount()))
        .isPositive();

    assertThat(samples.addSampleReports("citizen@test.example", "officer@test.example", ""))
        .isFalse();
    assertThat(sampleReports()).isEqualTo(added);
  }

  @Test
  void sampleAccountsCannotSignInAndNoAdministratorIsCreated() {
    samples.addSampleReports("citizen@test.example", "officer@test.example", "");
    var sampleAccounts =
        jdbc.queryForList(
            "SELECT password_hash FROM users WHERE email LIKE '%@sample.civicpulse.invalid'",
            String.class);
    assertThat(sampleAccounts).hasSizeGreaterThanOrEqualTo(20);
    for (String hash : sampleAccounts.stream().distinct().toList()) {
      assertThat(passwords.matches("CivicDemo!2026", hash)).isFalse();
      assertThat(passwords.matches("FeatureTest!2026", hash)).isFalse();
    }
    assertThat(count("SELECT COUNT(*) FROM users WHERE role='ADMIN'")).isEqualTo(1);
  }

  @Test
  void withoutConfiguredAccountsSampleAccountsTakeTheirPlace() {
    assertThat(samples.addSampleReports("", "", "")).isTrue();
    assertThat(sampleReports()).isGreaterThan(40);
    assertThat(dao.list("mine", citizen.id(), null, null, 0, 50)).isEmpty();
    assertThat(count("SELECT COUNT(*) FROM complaints WHERE assigned_officer_id=?", officer.id()))
        .isZero();
  }

  @Test
  void accountsWithTheWrongRoleAreNotUsed() {
    assertThat(samples.addSampleReports("admin@test.example", "citizen@test.example", ""))
        .isTrue();
    assertThat(count("SELECT COUNT(*) FROM complaints WHERE reporter_id=?", admin.id())).isZero();
    assertThat(count("SELECT COUNT(*) FROM complaints WHERE assigned_officer_id=?", citizen.id()))
        .isZero();
  }
}
