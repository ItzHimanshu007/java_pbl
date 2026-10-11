package com.civicpulse.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Offline gazetteer of Jaipur localities grouped into service zones. It keeps address search
 * useful when the external geocoder is slow or unavailable and lets reports be grouped by zone.
 * Zones are approximate groupings for routing and statistics, not legal ward boundaries.
 */
@Component
public class JaipurPlaces {
  public static final double CENTER_LATITUDE = 26.9124, CENTER_LONGITUDE = 75.7873;
  public static final String OTHER_AREAS = "Other areas";
  private static final double ZONE_RADIUS_METERS = 4500;

  public record Locality(String name, String hindi, String zone, double latitude, double longitude) {
    public String getName() {
      return name;
    }

    public String getHindi() {
      return hindi;
    }

    public String getZone() {
      return zone;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }
  }

  public record Zone(String name, String hindi, double latitude, double longitude, List<Locality> places) {
    public String getName() {
      return name;
    }

    public String getHindi() {
      return hindi;
    }

    public double getLatitude() {
      return latitude;
    }

    public double getLongitude() {
      return longitude;
    }

    public List<Locality> getPlaces() {
      return places;
    }
  }

  private record ZoneName(String name, String hindi) {}

  private record Document(List<ZoneName> zones, List<Locality> places) {}

  private final List<Locality> places;
  private final List<Zone> zones;
  private final Map<String, String> zoneHindi = new HashMap<>();

  public JaipurPlaces(ObjectMapper json) throws IOException {
    Document document;
    try (var in = new ClassPathResource("location/jaipur-places.json").getInputStream()) {
      document = json.readValue(in, Document.class);
    }
    places = List.copyOf(document.places());
    var zoneList = new ArrayList<Zone>();
    for (var z : document.zones()) {
      var members = places.stream().filter(p -> p.zone().equals(z.name())).toList();
      double lat = members.stream().mapToDouble(Locality::latitude).average().orElse(CENTER_LATITUDE);
      double lon =
          members.stream().mapToDouble(Locality::longitude).average().orElse(CENTER_LONGITUDE);
      zoneList.add(new Zone(z.name(), z.hindi(), lat, lon, members));
      zoneHindi.put(z.name(), z.hindi());
    }
    zoneHindi.put(OTHER_AREAS, "अन्य क्षेत्र");
    zones = List.copyOf(zoneList);
  }

  public List<Locality> all() {
    return places;
  }

  public List<Zone> zones() {
    return zones;
  }

  public String zoneHindi(String zone) {
    return zoneHindi.getOrDefault(zone, zone);
  }

  /** Readable address for a locality in the interface language. */
  public String label(Locality p, String language) {
    return "hi".equals(language)
        ? p.hindi() + ", " + zoneHindi(p.zone()) + ", जयपुर"
        : p.name() + ", " + p.zone() + ", Jaipur";
  }

  /** Localities whose English or Hindi name matches; prefix matches rank first. */
  public List<Place> search(String query, String language, int limit) {
    String q = normalize(query);
    if (q.length() < 2) return List.of();
    var prefix = new ArrayList<Locality>();
    var contains = new ArrayList<Locality>();
    for (var p : places) {
      String en = normalize(p.name()), hi = normalize(p.hindi()), zone = normalize(p.zone());
      if (en.startsWith(q) || hi.startsWith(q)) prefix.add(p);
      else if (en.contains(q) || hi.contains(q) || zone.startsWith(q)) contains.add(p);
    }
    prefix.addAll(contains);
    return prefix.stream()
        .limit(limit)
        .map(p -> new Place(label(p, language), p.latitude(), p.longitude()))
        .toList();
  }

  public Optional<Locality> nearest(double lat, double lon, double maxMeters) {
    Locality best = null;
    double bestDistance = Double.MAX_VALUE;
    for (var p : places) {
      double d = distance(lat, lon, p.latitude(), p.longitude());
      if (d < bestDistance) {
        best = p;
        bestDistance = d;
      }
    }
    return bestDistance <= maxMeters ? Optional.ofNullable(best) : Optional.empty();
  }

  /** Service zone of a coordinate, or {@link #OTHER_AREAS} outside the covered city. */
  public String zoneOf(double lat, double lon) {
    return nearest(lat, lon, ZONE_RADIUS_METERS).map(Locality::zone).orElse(OTHER_AREAS);
  }

  static String normalize(String value) {
    if (value == null) return "";
    var out = new StringBuilder();
    for (char c : value.strip().toLowerCase(Locale.ROOT).toCharArray())
      if (Character.isLetterOrDigit(c) || Character.getType(c) == Character.NON_SPACING_MARK
          || Character.getType(c) == Character.COMBINING_SPACING_MARK) out.append(c);
    return out.toString();
  }

  static double distance(double lat1, double lon1, double lat2, double lon2) {
    double a =
        Math.pow(Math.sin(Math.toRadians(lat2 - lat1) / 2), 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(Math.toRadians(lon2 - lon1) / 2), 2);
    a = Math.max(0, Math.min(1, a));
    return 6_371_000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}
