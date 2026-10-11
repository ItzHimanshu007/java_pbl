package com.civicpulse;

import static org.assertj.core.api.Assertions.*;

import com.civicpulse.resolution.*;
import com.civicpulse.shared.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResolutionIntegrationTest extends FeatureTestSupport {
  @Autowired ResolutionService resolution;

  private long assigned() throws Exception {
    long id = report();
    jdbc.update(
        "UPDATE complaints SET status='ASSIGNED',assigned_officer_id=? WHERE id=?",
        officer.id(),
        id);
    return id;
  }

  @Test
  void fullAssignedToVerifiedClosureKeepsEvidenceAndTimeline() throws Exception {
    long id = assigned();
    resolution.start(id, "Repair crew starts work", officer);
    resolution.progress(id, "Surface prepared for repair", officer);
    resolution.resolve(id, "Surface repaired and inspected", photo(), officer);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.RESOLVED);
    resolution.confirm(id, citizen);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.CLOSED);
    assertThat(dao.find(id).closedAt()).isNotNull();
    assertThat(dao.find(id).resolvedAt()).isNotNull();
    assertThat(images.list(id)).hasSize(2);
    assertThat(dao.events(id)).hasSize(5);
  }

  @Test
  void wrongOfficerCannotStartOrUploadEvidence() throws Exception {
    long id = assigned();
    assertThatThrownBy(() -> resolution.start(id, "Unauthorized work update", otherOfficer))
        .isInstanceOf(FeatureException.class);
    assertThatThrownBy(
            () -> resolution.resolve(id, "Unauthorized resolution", photo(), otherOfficer))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.ASSIGNED);
  }

  @Test
  void cannotSkipStartOrCloseAnUnresolvedIssue() throws Exception {
    long id = assigned();
    assertThatThrownBy(() -> resolution.resolve(id, "Try to skip work started", photo(), officer))
        .isInstanceOf(FeatureException.class);
    assertThatThrownBy(() -> resolution.confirm(id, citizen)).isInstanceOf(FeatureException.class);
  }

  @Test
  void resolutionRequiresPhotoAndRollsBackState() throws Exception {
    long id = assigned();
    resolution.start(id, "Starting the assigned work", officer);
    assertThatThrownBy(() -> resolution.resolve(id, "Repair claimed without image", null, officer))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.IN_PROGRESS);
    assertThat(images.list(id)).hasSize(1);
  }

  @Test
  void onlyReporterCanConfirmOrReopen() throws Exception {
    long id = assigned();
    resolution.start(id, "Starting repair at location", officer);
    resolution.resolve(id, "Completed work with evidence", photo(), officer);
    assertThatThrownBy(() -> resolution.confirm(id, other)).isInstanceOf(FeatureException.class);
    assertThatThrownBy(() -> resolution.reopen(id, "Not actually fixed yet", admin))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void reopeningRequiresReasonAndNewRepairBeforeClosure() throws Exception {
    long id = assigned();
    resolution.start(id, "Starting the repair work", officer);
    resolution.resolve(id, "First repair completed", photo(), officer);
    assertThatThrownBy(() -> resolution.reopen(id, "", citizen))
        .isInstanceOf(FeatureException.class);
    resolution.reopen(id, "The surface is still uneven", citizen);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.IN_PROGRESS);
    assertThatThrownBy(() -> resolution.confirm(id, citizen)).isInstanceOf(FeatureException.class);
    resolution.resolve(id, "Second repair completed", photo(), officer);
    resolution.confirm(id, citizen);
    assertThat(images.list(id)).hasSize(3);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.CLOSED);
  }

  @Test
  void repeatStaleActionsFailWithoutDuplicatingEvents() throws Exception {
    long id = assigned();
    resolution.start(id, "Starting work with evidence", officer);
    int count = dao.events(id).size();
    assertThatThrownBy(() -> resolution.start(id, "Duplicate start command", officer))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.events(id)).hasSize(count);
  }
}
