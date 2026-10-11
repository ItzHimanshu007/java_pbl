package com.civicpulse;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.civicpulse.shared.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportingIntegrationTest extends FeatureTestSupport {
  @Test
  void submissionSavesCanonicalPhotoReferenceAndInitialEvent() throws Exception {
    long id = report();
    var c = dao.find(id);
    assertThat(c.reference()).startsWith("CP-");
    assertThat(c.status()).isEqualTo(ComplaintStatus.REPORTED);
    assertThat(c.reporterId()).isEqualTo(citizen.id());
    assertThat(dao.events(id)).hasSize(1);
    assertThat(images.list(id)).hasSize(1);
    assertThat(images.bytes(images.list(id).get(0).id()))
        .startsWith(new byte[] {(byte) 137, 80, 78, 71});
  }

  @Test
  void creationFormAndOwnListAreCitizenOnly() throws Exception {
    mvc.perform(get("/citizen/reports/new").with(as(citizen))).andExpect(status().isOk());
    mvc.perform(get("/citizen/reports/new").with(as(officer))).andExpect(status().isForbidden());
    long id = report();
    mvc.perform(get("/citizen/reports").with(as(other))).andExpect(model().attribute("total", 0L));
    mvc.perform(get("/reports/" + id).with(as(other))).andExpect(status().isForbidden());
  }

  @Test
  void photoAccessChecksComplaintOwnershipAndAssignment() throws Exception {
    long id = report();
    long image = images.list(id).get(0).id();
    mvc.perform(get("/reports/images/" + image).with(as(citizen)))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/png"));
    mvc.perform(get("/reports/images/" + image).with(as(other))).andExpect(status().isForbidden());
    mvc.perform(get("/reports/images/" + image).with(as(otherOfficer)))
        .andExpect(status().isForbidden());
    mvc.perform(get("/reports/images/" + image).with(as(admin))).andExpect(status().isOk());
  }

  @Test
  void invalidUploadRollsBackReportAndHistory() {
    var bad =
        new MockMultipartFile("photo", "fake.png", "image/png", "<script>bad</script>".getBytes());
    assertThatThrownBy(() -> submission.submit(form(), bad, citizen))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.count("all", null, null, null)).isZero();
  }

  @Test
  void imageDimensionLimitCheckedBeforeDecoding() throws Exception {
    var b = new java.awt.image.BufferedImage(5001, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
    var out = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(b, "png", out);
    assertThatThrownBy(
            () ->
                images.validate(
                    new MockMultipartFile("photo", "wide.png", "image/png", out.toByteArray())))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void oversizedImageIsRejected() {
    assertThatThrownBy(
            () ->
                images.validate(
                    new MockMultipartFile(
                        "photo", "big.png", "image/png", new byte[5 * 1024 * 1024 + 1])))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void submittedReportsCannotBeEditedInAnyState() throws Exception {
    long id = report();
    var title = dao.find(id).title();
    assertThatThrownBy(() -> submission.edit(id, form(), citizen))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.find(id).title()).isEqualTo(title);
    assertThat(dao.events(id)).hasSize(1);
    mvc.perform(get("/citizen/reports/" + id + "/edit").with(as(citizen)))
        .andExpect(status().isConflict());
    mvc.perform(post("/citizen/reports/" + id + "/edit").with(as(citizen)).with(csrf()))
        .andExpect(status().isConflict());
  }

  @Test
  void titleReferenceSearchFiltersAndPagination() throws Exception {
    for (int n = 0; n < 12; n++) report();
    assertThat(dao.list("mine", citizen.id(), "REPORTED", "park", 0, 10)).hasSize(10);
    assertThat(dao.list("mine", citizen.id(), null, null, 1, 10)).hasSize(2);
    assertThat(dao.list("mine", other.id(), null, null, 0, 10)).isEmpty();
    assertThatThrownBy(() -> dao.list("all", null, "INVALID", null, 0, 10))
        .isInstanceOf(FeatureException.class);
  }

  @Test
  void invalidCoordinatesAndMissingPhotoDoNotPersist() {
    var f = form();
    f.setLatitude(Double.NaN);
    assertThatThrownBy(() -> submission.submit(f, null, citizen))
        .isInstanceOf(FeatureException.class);
    assertThatThrownBy(() -> submission.submit(form(), null, citizen))
        .isInstanceOf(FeatureException.class);
    assertThat(dao.count("all", null, null, null)).isZero();
  }

  @Test
  void postIgnoresPostedReporterAndStatus() throws Exception {
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
                .param("reporterId", Long.toString(other.id()))
                .param("status", "CLOSED"))
        .andExpect(status().is3xxRedirection());
    var c = dao.list("mine", citizen.id(), null, null, 0, 10).get(0);
    assertThat(c.status()).isEqualTo(ComplaintStatus.REPORTED);
    assertThat(c.reporterId()).isEqualTo(citizen.id());
  }

  @Test
  void creationWithoutCsrfRejected() throws Exception {
    mvc.perform(multipart("/citizen/reports").file(photo()).with(as(citizen)))
        .andExpect(status().isForbidden());
    assertThat(dao.count("all", null, null, null)).isZero();
  }
}
