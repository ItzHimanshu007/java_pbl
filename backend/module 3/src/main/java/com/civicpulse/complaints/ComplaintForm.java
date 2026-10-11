package com.civicpulse.complaints;

import jakarta.validation.constraints.*;

public class ComplaintForm {
  @Positive private Long relatedReportId;

  /** Hide the reporter's identity; only a keyed hash of the account is stored. */
  private boolean anonymous;

  public boolean isAnonymous() {
    return anonymous;
  }

  public void setAnonymous(boolean v) {
    anonymous = v;
  }

  public Long getRelatedReportId() {
    return relatedReportId;
  }

  public void setRelatedReportId(Long v) {
    relatedReportId = v;
  }

  @NotBlank
  @Size(min = 5, max = 120)
  private String title;

  @NotBlank
  @Size(min = 15, max = 2000)
  private String description;

  @NotBlank
  @Size(min = 5, max = 250)
  private String address;

  @NotNull @Positive private Long categoryId;

  @NotNull
  @DecimalMin("-90")
  @DecimalMax("90")
  private Double latitude;

  @NotNull
  @DecimalMin("-180")
  @DecimalMax("180")
  private Double longitude;

  public String getTitle() {
    return title;
  }

  public void setTitle(String v) {
    title = v;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String v) {
    description = v;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String v) {
    address = v;
  }

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long v) {
    categoryId = v;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double v) {
    latitude = v;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double v) {
    longitude = v;
  }

  public static ComplaintForm from(Complaint c) {
    var f = new ComplaintForm();
    f.title = c.title();
    f.description = c.description();
    f.address = c.address();
    f.categoryId = c.categoryId();
    f.latitude = c.latitude();
    f.longitude = c.longitude();
    return f;
  }
}
