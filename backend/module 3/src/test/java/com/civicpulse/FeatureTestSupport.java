package com.civicpulse;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import com.civicpulse.account.UserDao;
import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.Role;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public abstract class FeatureTestSupport {
  @Autowired protected JdbcTemplate jdbc;
  @Autowired protected UserDao users;
  @Autowired protected PasswordEncoder passwords;
  @Autowired protected ComplaintDao dao;
  @Autowired protected SubmissionService submission;
  @Autowired protected ImageStore images;
  @Autowired protected MockMvc mvc;
  protected CurrentUser citizen, other, officer, otherOfficer, admin;

  @BeforeEach
  void fixtures() {
    TestDataCleaner.clear(jdbc);
    citizen = account("citizen", Role.CITIZEN);
    other = account("other", Role.CITIZEN);
    officer = account("officer", Role.OFFICER);
    otherOfficer = account("second", Role.OFFICER);
    admin = account("admin", Role.ADMIN);
    jdbc.update(
        "UPDATE users SET department_id=? WHERE role='OFFICER'", dao.category(1).departmentId());
  }

  protected CurrentUser account(String name, Role role) {
    var u = users.create(name, name + "@test.example", passwords.encode("FeatureTest!2026"), role);
    return new CurrentUser(u.id(), u.displayName(), u.email(), u.role(), "test-token");
  }

  protected RequestPostProcessor as(CurrentUser u) {
    return authentication(
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            .authenticated(
                u,
                null,
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(
                    "ROLE_" + u.role().name())));
  }

  protected ComplaintForm form() {
    var f = new ComplaintForm();
    f.setTitle("Pothole near the park");
    f.setDescription("A damaged road surface needs repair near the park entrance.");
    f.setAddress("Community park entrance, Test City");
    f.setCategoryId(1L);
    f.setLatitude(26.9124);
    f.setLongitude(75.7873);
    return f;
  }

  protected MockMultipartFile photo() throws IOException {
    var b = new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);
    var out = new ByteArrayOutputStream();
    ImageIO.write(b, "png", out);
    return new MockMultipartFile("photo", "../unsafe-name.jpg", "image/jpeg", out.toByteArray());
  }

  protected long report() throws IOException {
    return submission.submit(form(), photo(), citizen);
  }
}
