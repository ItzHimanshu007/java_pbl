package com.civicpulse.shared;

/** Expected business failure. Messages are fixed text, never database details. */
public class FeatureException extends RuntimeException {
  private final int status;

  public FeatureException(int status, String message) {
    super(message);
    this.status = status;
  }

  public int status() {
    return status;
  }

  public static FeatureException conflict(String message) {
    return new FeatureException(409, message);
  }

  public static FeatureException forbidden() {
    return new FeatureException(403, "This report is not available to your account.");
  }

  public static FeatureException missing() {
    return new FeatureException(404, "This report could not be found.");
  }

  public static FeatureException invalid(String message) {
    return new FeatureException(400, message);
  }
}
