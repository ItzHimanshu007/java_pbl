package com.civicpulse.complaints;

public record Category(long id, String name, long departmentId) {
  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
