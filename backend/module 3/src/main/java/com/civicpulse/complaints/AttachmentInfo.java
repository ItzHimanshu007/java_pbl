package com.civicpulse.complaints;

public record AttachmentInfo(long id, String purpose) {
  public long getId() {
    return id;
  }

  public String getPurpose() {
    return purpose;
  }
}
