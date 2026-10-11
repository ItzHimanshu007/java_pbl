package com.civicpulse.location;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locations")
public class LocationController {
  private final Geocoder geocoder;

  public LocationController(Geocoder geocoder) {
    this.geocoder = geocoder;
  }

  @GetMapping("/search")
  public ResponseEntity<List<Place>> search(
      @RequestParam String q,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(geocoder.search(q, latitude, longitude));
  }

  @GetMapping("/reverse")
  public ResponseEntity<List<Place>> reverse(
      @RequestParam Double latitude, @RequestParam Double longitude) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(geocoder.reverse(latitude, longitude));
  }
}
