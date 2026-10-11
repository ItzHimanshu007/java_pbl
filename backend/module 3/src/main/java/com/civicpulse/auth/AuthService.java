package com.civicpulse.auth;

import com.civicpulse.account.AccountUser;
import com.civicpulse.account.UserDao;
import com.civicpulse.shared.Role;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserDao users;
  private final PasswordEncoder passwords;
  private final JwtTokenService tokens;

  public AuthService(UserDao users, PasswordEncoder passwords, JwtTokenService tokens) {
    this.users = users;
    this.passwords = passwords;
    this.tokens = tokens;
  }

  public static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }

  public static void checkPassword(String password) {
    if (password.length() < 10 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new IllegalArgumentException(
          "Use at least 10 characters and no more than 72 UTF-8 bytes for your password.");
    }
  }

  @Transactional
  public AccountUser register(RegistrationForm form) {
    checkPassword(form.getPassword());
    if (!form.getPassword().equals(form.getConfirmPassword()))
      throw new IllegalArgumentException("Passwords do not match.");
    String email = normalizeEmail(form.getEmail());
    if (users.findByEmail(email).isPresent())
      throw new DuplicateKeyException("Email already registered");
    return users.create(
        form.getDisplayName().strip(), email, passwords.encode(form.getPassword()), Role.CITIZEN);
  }

  public AccountUser authenticate(LoginForm form) {
    var user = users.findByEmail(normalizeEmail(form.getEmail()));
    if (user.isEmpty()
        || !passwords.matches(form.getPassword(), user.get().passwordHash())
        || !user.get().enabled()) {
      throw new IllegalArgumentException("Email or password is incorrect.");
    }
    return user.get();
  }

  @Transactional
  public String issueLogin(AccountUser user) {
    return tokens.issue(user);
  }
}
