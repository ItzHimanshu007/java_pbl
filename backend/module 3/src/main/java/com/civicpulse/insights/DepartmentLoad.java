package com.civicpulse.insights;

/** Open, overdue and verified-closed work for one department. */
public record DepartmentLoad(String name, long open, long overdue, long closed, int percent) {
  public String getName() {
    return name;
  }

  public long getOpen() {
    return open;
  }

  public long getOverdue() {
    return overdue;
  }

  public long getClosed() {
    return closed;
  }

  public int getPercent() {
    return percent;
  }
}
