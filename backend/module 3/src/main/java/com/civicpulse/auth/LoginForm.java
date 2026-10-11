package com.civicpulse.auth;

import jakarta.validation.constraints.*;

public class LoginForm {
  @NotBlank
  @Email
  @Size(max = 190)
  private String email;

  @NotBlank
  @Size(max = 72)
  private String password;

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    email = value;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    password = value;
  }
}
