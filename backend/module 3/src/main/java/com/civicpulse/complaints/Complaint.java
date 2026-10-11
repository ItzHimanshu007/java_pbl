package com.civicpulse.complaints;

import com.civicpulse.shared.ComplaintStatus;
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
    Instant closedAt) {
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
}
