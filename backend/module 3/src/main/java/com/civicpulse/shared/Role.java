package com.civicpulse.shared;

public enum Role {
  CITIZEN("Citizen", "/citizen/dashboard"),
  OFFICER("Officer", "/officer/dashboard"),
  ADMIN("Administrator", "/admin/dashboard");
  private final String label;
  private final String dashboard;

  Role(String label, String dashboard) {
    this.label = label;
    this.dashboard = dashboard;
  }

  public String label() {
    return label;
  }

  public String dashboard() {
    return dashboard;
  }
}
