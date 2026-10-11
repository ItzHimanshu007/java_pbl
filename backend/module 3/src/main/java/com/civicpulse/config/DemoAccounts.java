package com.civicpulse.config;

import com.civicpulse.account.UserDao;
import com.civicpulse.shared.Role;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(10)
public class DemoAccounts implements ApplicationRunner {
  private final UserDao users;
  private final PasswordEncoder passwords;

  public DemoAccounts(UserDao users, PasswordEncoder passwords) {
    this.users = users;
    this.passwords = passwords;
  }

  @Override
  public void run(ApplicationArguments args) {
    for (Role role : Role.values()) {
      String email = role.name().toLowerCase(java.util.Locale.ROOT) + "@civicpulse.demo";
      if (users.findByEmail(email).isEmpty())
        users.create("Demo " + role.label(), email, passwords.encode("CivicDemo!2026"), role);
    }
  }
}
