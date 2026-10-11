package com.civicpulse.account;

import java.time.LocalDate;

public record AccountProfile(
    LocalDate dateOfBirth,
    String phone,
    String address,
    String city,
    String postalCode,
    String neighbourhood,
    String language,
    String theme,
    Double homeLatitude,
    Double homeLongitude) {
  public static AccountProfile empty() {
    return new AccountProfile(null, "", "", "", "", "", "en", "system", null, null);
  }
}
