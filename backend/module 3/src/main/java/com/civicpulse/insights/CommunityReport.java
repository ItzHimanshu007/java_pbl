package com.civicpulse.insights;

/** Explicit safe map DTO: no reporter identity, email, description or private evidence. */
public record CommunityReport(
    long id,
    String reference,
    String title,
    String category,
    String address,
    double latitude,
    double longitude,
    String status,
    long supports,
    boolean supported,
    boolean mine,
    double distanceMeters,
    long categoryId,
    long issueRootId,
    long repeatCount,
    String priority,
    int progress,
    String categoryCode,
    String zone,
    String createdAt) {
  public long getRepeatCount() {
    return repeatCount;
  }

  public String getPriority() {
    return priority;
  }

  public int getProgress() {
    return progress;
  }

  public long getIssueRootId() {
    return issueRootId;
  }

  public long getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public String getTitle() {
    return title;
  }

  public String getCategory() {
    return category;
  }

  public String getAddress() {
    return address;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public String getStatus() {
    return status;
  }

  public long getSupports() {
    return supports;
  }

  public boolean isSupported() {
    return supported;
  }

  public boolean isMine() {
    return mine;
  }

  public String getCategoryCode() {
    return categoryCode == null || categoryCode.isBlank() ? "other" : categoryCode;
  }

  public String getZone() {
    return zone;
  }

  public String getCreatedAt() {
    return createdAt;
  }
}
