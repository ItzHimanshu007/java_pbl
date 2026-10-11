package com.civicpulse.complaints;

import java.time.Instant;

public record ComplaintEvent(
    long id,
    String actor,
    String action,
    String fromStatus,
    String toStatus,
    String note,
    Instant createdAt) {
  public String getActor() {
    return actor;
  }

  public String getAction() {
    return action.replace('_', ' ');
  }

  /** Dictionary key for the timeline label, e.g. event.WORK_STARTED. */
  public String getLabelKey() {
    return "event." + action;
  }

  public String getToStatus() {
    return toStatus;
  }

  public String getNote() {
    return note;
  }

  public String getCreatedAt() {
    return createdAt.toString();
  }
}
