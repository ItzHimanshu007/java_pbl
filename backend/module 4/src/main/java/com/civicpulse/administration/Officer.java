package com.civicpulse.administration;

public record Officer(
    long id, String name, String email, long departmentId, String department, boolean enabled) {
  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public long getDepartmentId() {
    return departmentId;
  }

  public String getDepartment() {
    return department;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
