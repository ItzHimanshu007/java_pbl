package com.civicpulse.administration;

import org.springframework.boot.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(20)
public class DemoOfficerDepartment implements ApplicationRunner {
  private final JdbcTemplate jdbc;

  public DemoOfficerDepartment(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void run(ApplicationArguments args) {
    jdbc.update(
        "UPDATE users SET department_id=(SELECT id FROM departments WHERE name='Roads and"
            + " Infrastructure') WHERE email='officer@civicpulse.demo' AND role='OFFICER'");
  }
}
