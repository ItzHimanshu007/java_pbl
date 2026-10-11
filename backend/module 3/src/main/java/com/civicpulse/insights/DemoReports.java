package com.civicpulse.insights;

import com.civicpulse.account.UserDao;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(100)
public class DemoReports implements ApplicationRunner {
  private final ComplaintDao dao;
  private final ImageStore images;
  private final UserDao users;
  private final JdbcTemplate jdbc;

  public DemoReports(ComplaintDao dao, ImageStore images, UserDao users, JdbcTemplate jdbc) {
    this.dao = dao;
    this.images = images;
    this.users = users;
    this.jdbc = jdbc;
  }

  public void run(ApplicationArguments args) throws Exception {
    if (dao.count("all", null, null, null) > 0) return;
    long citizen = users.findByEmail("citizen@civicpulse.demo").orElseThrow().id();
    long officer = users.findByEmail("officer@civicpulse.demo").orElseThrow().id();
    long admin = users.findByEmail("admin@civicpulse.demo").orElseThrow().id();
    seedCommunity(citizen, officer, admin);
    String[] titles = {
      "Pothole near the community park",
      "Uneven paving at the crossing",
      "Repair ready for citizen review",
      "Street surface restored and verified"
    };
    ComplaintStatus[] states = {
      ComplaintStatus.REPORTED,
      ComplaintStatus.ASSIGNED,
      ComplaintStatus.RESOLVED,
      ComplaintStatus.CLOSED
    };
    for (int i = 0; i < 4; i++) {
      var f = new ComplaintForm();
      f.setTitle(titles[i]);
      f.setDescription(
          "Illustrative local demonstration report. Inspect the location and restore a safe surface"
              + " for pedestrians and vehicles.");
      f.setAddress("Demonstration community block " + (i + 1) + ", Jaipur");
      f.setCategoryId(1L);
      f.setLatitude(26.9124 + i * .0013);
      f.setLongitude(75.7873 + i * .0010);
      long id = dao.create(f, citizen, "CP-DEMO-000" + (i + 1));
      images.add(id, citizen, "ORIGINAL", picture(false));
      dao.event(
          id,
          citizen,
          "REPORTED",
          null,
          ComplaintStatus.REPORTED,
          "Demonstration report submitted.");
      if (i > 0) {
        dao.transition(id, ComplaintStatus.ASSIGNED, officer);
        dao.event(
            id,
            admin,
            "ASSIGNED",
            ComplaintStatus.REPORTED,
            ComplaintStatus.ASSIGNED,
            "Roads team assigned for this demonstration.");
      }
      if (i > 1) {
        dao.transition(id, ComplaintStatus.IN_PROGRESS, officer);
        dao.event(
            id,
            officer,
            "WORK_STARTED",
            ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS,
            "Crew began repairing the surface.");
        images.add(id, officer, "REPAIR", picture(true));
        dao.transition(id, ComplaintStatus.RESOLVED, officer);
        dao.event(
            id,
            officer,
            "REPAIR_SUBMITTED",
            ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.RESOLVED,
            "Repair evidence uploaded for citizen review.");
      }
      if (i == 3) {
        dao.transition(id, ComplaintStatus.CLOSED, officer);
        dao.event(
            id,
            citizen,
            "CITIZEN_CONFIRMED",
            ComplaintStatus.RESOLVED,
            ComplaintStatus.CLOSED,
            "Citizen confirmed the demonstration repair.");
      }
      jdbc.update(
          "UPDATE complaints SET created_at=? WHERE id=?",
          Timestamp.from(Instant.now().minusSeconds((i + 1) * 86400)),
          id);
    }
  }

  private void seedCommunity(long citizen, long officer, long admin) throws Exception {
    long[] people = new long[7];
    var hash = users.findById(citizen).orElseThrow().passwordHash();
    for (int i = 0; i < people.length; i++)
      people[i] =
          users
              .create(
                  "Sample Citizen " + (i + 1),
                  "sample" + (i + 1) + "@civicpulse.demo",
                  hash,
                  Role.CITIZEN)
              .id();
    long root = 0;
    for (int i = 0; i < 5; i++) {
      long id =
          sample(
              "Damaged road beside Central Park",
              "Prithviraj Road, Central Park, Jaipur",
              1,
              26.9067 + i * .00002,
              75.8072 + i * .00002,
              people[i],
              "CP-DEMO-HIGH-" + i);
      if (i == 0) root = id;
      else dao.link(id, root);
      dao.transition(id, ComplaintStatus.ASSIGNED, officer);
      dao.event(
          id,
          admin,
          "ASSIGNED",
          ComplaintStatus.REPORTED,
          ComplaintStatus.ASSIGNED,
          "Roads team assigned.");
      dao.transition(id, ComplaintStatus.IN_PROGRESS, officer);
      dao.event(
          id,
          officer,
          "WORK_STARTED",
          ComplaintStatus.ASSIGNED,
          ComplaintStatus.IN_PROGRESS,
          "Surface inspection and preparation started.");
    }
    long medium =
        sample(
            "Waste collection needed near the market",
            "MI Road, Panch Batti, Jaipur",
            3,
            26.9165,
            75.8054,
            people[5],
            "CP-DEMO-MED-1");
    long repeat =
        sample(
            "Waste collection needed near the market",
            "MI Road, Panch Batti, Jaipur",
            3,
            26.91652,
            75.80542,
            people[6],
            "CP-DEMO-MED-2");
    dao.link(repeat, medium);
    sample(
        "Streetlight not working at the crossing",
        "C Scheme, Jaipur",
        2,
        26.9105,
        75.7990,
        people[2],
        "CP-DEMO-LOW-1");
    sample(
        "Drain blocked after rainfall",
        "Bapu Nagar, Jaipur",
        5,
        26.8875,
        75.8192,
        people[4],
        "CP-DEMO-LOW-2");
  }

  private long sample(
      String title,
      String address,
      long category,
      double lat,
      double lon,
      long citizen,
      String reference)
      throws Exception {
    var f = new ComplaintForm();
    f.setTitle(title);
    f.setAddress(address);
    f.setCategoryId(category);
    f.setLatitude(lat);
    f.setLongitude(lon);
    f.setDescription(
        "Sample community issue for exploring the report workflow. These are fictional reports.");
    long id = dao.create(f, citizen, reference);
    if (category == 1) images.add(id, citizen, "ORIGINAL", picture(false));
    dao.event(id, citizen, "REPORTED", null, ComplaintStatus.REPORTED, "Sample report submitted.");
    return id;
  }

  private byte[] picture(boolean repaired) throws Exception {
    try (var in =
        new org.springframework.core.io.ClassPathResource(
                "demo/photos/" + (repaired ? "after.jpg" : "before.jpg"))
            .getInputStream()) {
      var image = javax.imageio.ImageIO.read(in);
      var out = new java.io.ByteArrayOutputStream();
      javax.imageio.ImageIO.write(image, "png", out);
      return out.toByteArray();
    }
  }
}
