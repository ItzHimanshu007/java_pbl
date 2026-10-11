package com.civicpulse.administration;

import jakarta.validation.constraints.*;

public class OfficerForm {
  @NotBlank
  @Size(min = 2, max = 80)
  private String displayName;

  @NotBlank
  @Email
  @Size(max = 190)
  private String email;

  @NotBlank
  @Size(min = 10, max = 72)
  private String password;

  @NotNull @Positive private Long departmentId;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String v) {
    displayName = v;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String v) {
    email = v;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String v) {
    password = v;
  }

  public Long getDepartmentId() {
    return departmentId;
  }

  public void setDepartmentId(Long v) {
    departmentId = v;
  }
}
