package com.civicpulse.complaints;

public record Category(long id, String name, long departmentId, String code, int slaHours) {
  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getDepartmentId() {
    return departmentId;
  }

  public String getCode() {
    return code == null || code.isBlank() ? "other" : code;
  }

  /** Target resolution time in hours. */
  public int getSlaHours() {
    return slaHours;
  }

  /** Whole days when the target is at least two days, otherwise hours (see {@link #getSlaUnit}). */
  public int getSlaAmount() {
    return inDays() ? slaHours / 24 : slaHours;
  }

  public String getSlaUnit() {
    return inDays() ? "days" : "hours";
  }

  private boolean inDays() {
    return slaHours >= 48 && slaHours % 24 == 0;
  }
}
