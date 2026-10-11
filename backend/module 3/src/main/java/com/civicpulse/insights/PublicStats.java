package com.civicpulse.insights;

/** City-wide transparency figures shown on the public home page. No personal data. */
public record PublicStats(
    long total,
    long awaiting,
    long inProgress,
    long resolved,
    int resolvedPercent,
    String averageHours,
    int categories,
    int zones) {
  public long getTotal() {
    return total;
  }

  public long getAwaiting() {
    return awaiting;
  }

  public long getInProgress() {
    return inProgress;
  }

  public long getResolved() {
    return resolved;
  }

  public int getResolvedPercent() {
    return resolvedPercent;
  }

  public String getAverageHours() {
    return averageHours;
  }

  public int getCategories() {
    return categories;
  }

  public int getZones() {
    return zones;
  }
}
