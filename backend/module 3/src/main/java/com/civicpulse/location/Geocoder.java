package com.civicpulse.location;

import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Geocoder {
  private final String base;
  private final ObjectMapper json;
  private final LinkedHashMap<String, Cached> cache = new LinkedHashMap<>();
  private long lastRequest;

  private record Cached(long time, List<Place> places) {}

  public Geocoder(
      @Value("${civic.geocoder.url:https://photon.komoot.io}") String base, ObjectMapper json) {
    URI uri = URI.create(base);
    if (uri.getHost() == null
        || uri.getUserInfo() != null
        || uri.getQuery() != null
        || uri.getFragment() != null
        || !("https".equals(uri.getScheme())
            || "http".equals(uri.getScheme())
                && List.of("localhost", "127.0.0.1", "::1").contains(uri.getHost())))
      throw new IllegalArgumentException("Geocoder must use HTTPS, or a local HTTP server.");
    this.base = base.replaceAll("/+$", "");
    this.json = json;
  }

  public List<Place> search(String query, Double lat, Double lon) {
    String q = query == null ? "" : query.strip();
    if (q.length() < 3 || q.length() > 150)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    String bias = "";
    if (lat != null || lon != null) {
      valid(lat, lon);
      bias = "&lat=" + lat + "&lon=" + lon;
    }
    return request(
        "/api/?limit=5&lang=en&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) + bias);
  }

  public List<Place> reverse(Double lat, Double lon) {
    valid(lat, lon);
    return request("/reverse?limit=1&lang=en&lat=" + lat + "&lon=" + lon);
  }

  private void valid(Double lat, Double lon) {
    if (lat == null
        || lon == null
        || !Double.isFinite(lat)
        || !Double.isFinite(lon)
        || Math.abs(lat) > 90
        || Math.abs(lon) > 180) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
  }

  private List<Place> request(String path) {
    long now = System.currentTimeMillis();
    synchronized (cache) {
      var old = cache.get(path);
      if (old != null && now - old.time() < 300000) return old.places();
      if (now - lastRequest < 1000) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
      lastRequest = now;
    }
    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) URI.create(base + path).toURL().openConnection();
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(7000);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestProperty("Accept", "application/json");
      connection.setRequestProperty("User-Agent", "CivicPulse/1.1 (address search)");
      if (connection.getResponseCode() != 200)
        throw new java.io.IOException("Geocoder unavailable");
      byte[] bytes;
      try (var in = connection.getInputStream()) {
        bytes = in.readNBytes(131073);
      }
      if (bytes.length > 131072) throw new java.io.IOException("Geocoder response too large");
      List<Place> places = new ArrayList<>();
      for (var f : json.readTree(bytes).path("features")) {
        var c = f.path("geometry").path("coordinates");
        var p = f.path("properties");
        if (c.size() < 2 || !c.get(0).isNumber() || !c.get(1).isNumber()) continue;
        double lon = c.get(0).asDouble(), lat = c.get(1).asDouble();
        valid(lat, lon);
        var parts = new LinkedHashSet<String>();
        for (String key :
            List.of(
                "name",
                "housenumber",
                "street",
                "district",
                "city",
                "state",
                "postcode",
                "country")) {
          String value = p.path(key).asText("");
          if (!value.isBlank()) parts.add(value);
        }
        String label = String.join(", ", parts);
        if (label.isBlank()) continue;
        places.add(new Place(label.substring(0, Math.min(250, label.length())), lat, lon));
        if (places.size() == 5) break;
      }
      List<Place> result = List.copyOf(places);
      synchronized (cache) {
        cache.put(path, new Cached(now, result));
        while (cache.size() > 256) cache.remove(cache.keySet().iterator().next());
      }
      return result;
    } catch (Exception ex) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Address search is temporarily unavailable.");
    } finally {
      if (connection != null) connection.disconnect();
    }
  }
}
