package com.civicpulse.config;

import com.civicpulse.account.UserDao;
import com.civicpulse.auth.AuthService;
import com.civicpulse.shared.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(10) // before sample reports, which need an administrator
public class AccountBootstrap implements ApplicationRunner {
  private final UserDao users;
  private final PasswordEncoder passwords;
  private final String email, password, name;

  public AccountBootstrap(
      UserDao users,
      PasswordEncoder passwords,
      @Value("${civic.bootstrap.email:}") String email,
      @Value("${civic.bootstrap.password:}") String password,
      @Value("${civic.bootstrap.display-name:Administrator}") String name) {
    this.users = users;
    this.passwords = passwords;
    this.email = email;
    this.password = password;
    this.name = name;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (email.isBlank() && password.isBlank()) return;
    if (email.isBlank() || password.isBlank() || !email.matches("^[^ @]+@[^ @]+[.][^ @]+$")) {
      throw new IllegalStateException(
          "Initial administrator requires a valid CP_ADMIN_EMAIL and CP_ADMIN_PASSWORD");
    }
    AuthService.checkPassword(password);
    String normalized = AuthService.normalizeEmail(email);
    var existing = users.findByEmail(normalized);
    if (existing.isPresent()) {
      if (existing.get().role() != Role.ADMIN)
        throw new IllegalStateException("Bootstrap email belongs to a non-administrator account");
      return;
    }
    users.create(name.strip(), normalized, passwords.encode(password), Role.ADMIN);
  }
}
