package com.civicpulse.administration;

public record Department(long id, String name) {
  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
