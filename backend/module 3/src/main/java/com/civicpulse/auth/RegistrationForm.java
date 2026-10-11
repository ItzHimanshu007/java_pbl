package com.civicpulse.auth;

import jakarta.validation.constraints.*;

public class RegistrationForm {
  @NotBlank
  @Size(max = 80)
  private String displayName;

  @NotBlank
  @Email
  @Size(max = 190)
  private String email;

  @NotBlank
  @Size(min = 10, max = 72)
  private String password;

  @NotBlank private String confirmPassword;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String value) {
    displayName = value;
  }

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

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String value) {
    confirmPassword = value;
  }
}
