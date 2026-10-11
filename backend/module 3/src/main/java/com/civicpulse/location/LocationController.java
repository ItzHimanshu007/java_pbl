package com.civicpulse.location;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/locations")
public class LocationController {
  private static final int LIMIT = 8;
  private final Geocoder geocoder;
  private final JaipurPlaces jaipur;

  public LocationController(Geocoder geocoder, JaipurPlaces jaipur) {
    this.geocoder = geocoder;
    this.jaipur = jaipur;
  }

  /**
   * Jaipur localities are matched offline first; the external geocoder adds street-level results
   * when it is reachable. A provider outage still returns the local matches.
   */
  @GetMapping("/search")
  public ResponseEntity<List<Place>> search(
      @RequestParam String q,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude,
      @RequestParam(defaultValue = "en") String lang) {
    String language = "hi".equals(lang) ? "hi" : "en";
    var results = new ArrayList<>(jaipur.search(q, language, 5));
    try {
      boolean biased = latitude != null || longitude != null;
      for (var place :
          geocoder.search(
              q,
              biased ? latitude : JaipurPlaces.CENTER_LATITUDE,
              biased ? longitude : JaipurPlaces.CENTER_LONGITUDE)) {
        if (results.size() >= LIMIT) break;
        if (results.stream().noneMatch(p -> p.label().equalsIgnoreCase(place.label())))
          results.add(place);
      }
    } catch (ResponseStatusException ex) {
      if (results.isEmpty()) throw ex;
    }
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(results);
  }

  @GetMapping("/reverse")
  public ResponseEntity<List<Place>> reverse(
      @RequestParam Double latitude,
      @RequestParam Double longitude,
      @RequestParam(defaultValue = "en") String lang) {
    List<Place> places;
    try {
      places = geocoder.reverse(latitude, longitude);
    } catch (ResponseStatusException ex) {
      if (ex.getStatusCode().value() == 400) throw ex;
      places = List.of();
    }
    if (places.isEmpty()) places = nearby(latitude, longitude, "hi".equals(lang) ? "hi" : "en");
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(places);
  }

  /** Nearest known locality, keeping the exact selected coordinates. */
  private List<Place> nearby(double latitude, double longitude, String language) {
    return jaipur
        .nearest(latitude, longitude, 2000)
        .map(
            p ->
                List.of(
                    new Place(
                        ("hi".equals(language) ? "निकट " : "Near ") + jaipur.label(p, language),
                        latitude,
                        longitude)))
        .orElse(List.of());
  }
}
