package com.civicpulse.complaints;

import com.civicpulse.shared.ComplaintStatus;
import java.time.Duration;
import java.time.Instant;

public record Complaint(
    long id,
    String reference,
    long reporterId,
    long categoryId,
    long departmentId,
    String category,
    String department,
    String title,
    String description,
    String address,
    double latitude,
    double longitude,
    ComplaintStatus status,
    Long assignedOfficerId,
    String officerName,
    Instant createdAt,
    Instant updatedAt,
    Instant resolvedAt,
    Instant closedAt,
    String categoryCode,
    int slaHours,
    boolean anonymous,
    String reporterHash) {
  public long getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public String getCategory() {
    return category;
  }

  public long getDepartmentId() {
    return departmentId;
  }

  public long getCategoryId() {
    return categoryId;
  }

  public String getDepartment() {
    return department;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
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
    return status.name();
  }

  public Long getAssignedOfficerId() {
    return assignedOfficerId;
  }

  public String getOfficerName() {
    return officerName;
  }

  public String getCreatedAt() {
    return createdAt.toString();
  }

  public String getUpdatedAt() {
    return updatedAt.toString();
  }

  public boolean isEditable() {
    return false;
  }

  public String getCategoryCode() {
    return categoryCode == null || categoryCode.isBlank() ? "other" : categoryCode;
  }

  public int getSlaHours() {
    return slaHours;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  /** Target resolution time from the category's service level. */
  public Instant dueAt() {
    return createdAt.plus(Duration.ofHours(Math.max(1, slaHours)));
  }

  public String getDueAt() {
    return dueAt().toString();
  }

  /** Still waiting on the department after its target resolution time. */
  public boolean isOverdue() {
    return (status == ComplaintStatus.REPORTED
            || status == ComplaintStatus.ASSIGNED
            || status == ComplaintStatus.IN_PROGRESS)
        && Instant.now().isAfter(dueAt());
  }

  public boolean isActive() {
    return status != ComplaintStatus.CLOSED && status != ComplaintStatus.REJECTED;
  }

  /** Position in Reported → Assigned → In progress → Resolved → Closed (0-4). */
  public int getStep() {
    return switch (status) {
      case REPORTED, REJECTED -> 0;
      case ASSIGNED -> 1;
      case IN_PROGRESS -> 2;
      case RESOLVED -> 3;
      case CLOSED -> 4;
    };
  }

  public int getProgress() {
    return com.civicpulse.insights.InsightsService.progress(status.name());
  }
}
