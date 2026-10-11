package com.civicpulse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.administration.*;
import com.civicpulse.shared.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministrationIntegrationTest extends FeatureTestSupport {
  @Autowired AdministrationService administration;

  @Test
  void officerCreationUsesHashedPasswordAndDepartment() {
    var f = new OfficerForm();
    f.setDisplayName("New Officer");
    f.setEmail("NEW@EXAMPLE.COM");
    f.setPassword("NewOfficer!2026");
    f.setDepartmentId(dao.category(1).departmentId());
    administration.createOfficer(f, admin);
    var u = users.findByEmail("new@example.com").orElseThrow();
    assertThat(u.role()).isEqualTo(Role.OFFICER);
    assertThat(passwords.matches(f.getPassword(), u.passwordHash())).isTrue();
    assertThat(administration.officers())
        .anyMatch(o -> o.id() == u.id() && o.departmentId() == f.getDepartmentId());
  }

  @Test
  void citizenCannotCreateOfficerOrViewOfficerPage() throws Exception {
    mvc.perform(get("/admin/officers").with(as(citizen))).andExpect(status().isForbidden());
    assertThatThrownBy(() -> administration.createOfficer(new OfficerForm(), citizen))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void assignmentAndReassignmentWriteEvents() throws Exception {
    long id = report();
    administration.assign(
        id,
        officer.id(),
        dao.category(1).departmentId(),
        "Review complete; inspect the road.",
        admin);
    assertThat(dao.find(id).assignedOfficerId()).isEqualTo(officer.id());
    administration.assign(
        id,
        otherOfficer.id(),
        dao.category(1).departmentId(),
        "Reassign to the second officer.",
        admin);
    assertThat(dao.find(id).assignedOfficerId()).isEqualTo(otherOfficer.id());
    assertThat(dao.events(id)).hasSize(3);
  }

  @Test
  void wrongDepartmentAndDisabledOfficerCannotBeAssigned() throws Exception {
    long id = report();
    assertThatThrownBy(
            () ->
                administration.assign(
                    id,
                    officer.id(),
                    dao.category(2).departmentId(),
                    "Assign to wrong department",
                    admin))
        .isInstanceOf(FeatureException.class);
    administration.setEnabled(officer.id(), false, admin);
    assertThatThrownBy(
            () ->
                administration.assign(
                    id,
                    officer.id(),
                    dao.category(1).departmentId(),
                    "Disabled officer assignment",
                    admin))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.REPORTED);
    assertThat(dao.events(id)).hasSize(1);
  }

  @Test
  void activeOfficerCannotBeDisabled() throws Exception {
    long id = report();
    administration.assign(
        id, officer.id(), dao.category(1).departmentId(), "Work is assigned to officer", admin);
    assertThatThrownBy(() -> administration.setEnabled(officer.id(), false, admin))
        .isInstanceOf(FeatureException.class);
    assertThat(users.findById(officer.id()).orElseThrow().enabled()).isTrue();
  }

  @Test
  void rejectionRequiresReasonAndOnlyReportedState() throws Exception {
    long id = report();
    assertThatThrownBy(() -> administration.reject(id, "", admin))
        .isInstanceOf(FeatureException.class);
    administration.reject(id, "Duplicate invalid issue with clear reason", admin);
    assertThat(dao.find(id).status()).isEqualTo(ComplaintStatus.REJECTED);
    assertThatThrownBy(
            () ->
                administration.assign(
                    id,
                    officer.id(),
                    dao.category(1).departmentId(),
                    "Reassign rejected report",
                    admin))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void nonAdminCannotAssignOrReject() throws Exception {
    long id = report();
    assertThatThrownBy(
            () ->
                administration.assign(
                    id,
                    officer.id(),
                    dao.category(1).departmentId(),
                    "Citizen cannot assign",
                    citizen))
        .isInstanceOf(FeatureException.class);
    assertThatThrownBy(() -> administration.reject(id, "Officer cannot reject", officer))
        .isInstanceOf(FeatureException.class);
  }
}
