package com.civicpulse.account;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class ProfileForm {
  @NotBlank
  @Size(max = 80)
  private String displayName;

  @Past
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dateOfBirth;

  @Pattern(regexp = "^$|[+0-9 ()-]{7,24}")
  private String phone = "";

  @Size(max = 250)
  private String address = "";

  @Size(max = 80)
  private String city = "";

  @Size(max = 16)
  @Pattern(regexp = "[A-Za-z0-9 -]*")
  private String postalCode = "";

  @Size(max = 100)
  private String neighbourhood = "";

  @NotNull
  @Pattern(regexp = "en|hi")
  private String language = "en";

  @NotNull
  @Pattern(regexp = "light|dark|system")
  private String theme = "system";

  @DecimalMin("-90")
  @DecimalMax("90")
  private Double homeLatitude;

  @DecimalMin("-180")
  @DecimalMax("180")
  private Double homeLongitude;

  @AssertTrue(message = "Choose a valid home location.")
  public boolean isLocationValid() {
    return homeLatitude == null && homeLongitude == null
        || homeLatitude != null
            && homeLongitude != null
            && Double.isFinite(homeLatitude)
            && Double.isFinite(homeLongitude);
  }

  public static ProfileForm from(String name, AccountProfile p) {
    var f = new ProfileForm();
    f.displayName = name;
    f.dateOfBirth = p.dateOfBirth();
    f.phone = p.phone();
    f.address = p.address();
    f.city = p.city();
    f.postalCode = p.postalCode();
    f.neighbourhood = p.neighbourhood();
    f.language = p.language();
    f.theme = p.theme();
    f.homeLatitude = p.homeLatitude();
    f.homeLongitude = p.homeLongitude();
    return f;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String v) {
    displayName = v;
  }

  public LocalDate getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(LocalDate v) {
    dateOfBirth = v;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String v) {
    phone = v;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String v) {
    address = v;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String v) {
    city = v;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String v) {
    postalCode = v;
  }

  public String getNeighbourhood() {
    return neighbourhood;
  }

  public void setNeighbourhood(String v) {
    neighbourhood = v;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String v) {
    language = v;
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme(String v) {
    theme = v;
  }

  public Double getHomeLatitude() {
    return homeLatitude;
  }

  public void setHomeLatitude(Double v) {
    homeLatitude = v;
  }

  public Double getHomeLongitude() {
    return homeLongitude;
  }

  public void setHomeLongitude(Double v) {
    homeLongitude = v;
  }
}
