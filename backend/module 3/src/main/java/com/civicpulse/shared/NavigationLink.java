package com.civicpulse.shared;

public record NavigationLink(String label, String path) {
  public String getLabel() {
    return label;
  }

  public String getPath() {
    return path;
  }
}
