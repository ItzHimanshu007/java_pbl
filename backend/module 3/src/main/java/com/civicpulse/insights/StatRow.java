package com.civicpulse.insights;

/** One labelled count with its share of the largest row (0-100), for bar charts. */
public record StatRow(String label, String code, long value, int percent) {
  public String getLabel() {
    return label;
  }

  public String getCode() {
    return code;
  }

  public long getValue() {
    return value;
  }

  public int getPercent() {
    return percent;
  }
}
