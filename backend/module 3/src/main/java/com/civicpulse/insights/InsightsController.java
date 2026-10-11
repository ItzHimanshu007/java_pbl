package com.civicpulse.insights;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.location.JaipurPlaces;
import com.civicpulse.shared.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class InsightsController {
  private final InsightsService service;
  private final com.civicpulse.account.ProfileDao profiles;
  private final JaipurPlaces places;

  public InsightsController(
      InsightsService service, com.civicpulse.account.ProfileDao profiles, JaipurPlaces places) {
    this.service = service;
    this.profiles = profiles;
    this.places = places;
  }

  /** Public 311-style status lookup by reference number; no sign-in or identity needed. */
  @GetMapping("/track")
  public String track(@RequestParam(required = false) String ref, Model m) {
    m.addAttribute("ref", ref == null ? "" : ref.strip());
    if (ref != null && !ref.isBlank()) {
      try {
        var report = service.track(ref);
        m.addAttribute("report", report);
        m.addAttribute("zone", places.zoneOf(report.latitude(), report.longitude()));
        m.addAttribute("updates", service.publicUpdates(report.id()));
      } catch (FeatureException ex) {
        m.addAttribute("notFound", true);
      }
    }
    return "track";
  }

  @GetMapping({"/community", "/feed"})
  public String community(
      @AuthenticationPrincipal CurrentUser u,
      Model m,
      @RequestParam(required = false) Double latitude,
      @RequestParam(required = false) Double longitude,
      @RequestParam(defaultValue = "5") int radius,
      jakarta.servlet.http.HttpServletRequest request) {
    boolean feed = request.getRequestURI().endsWith("/feed");
    if (latitude == null && longitude == null) {
      var p = profiles.find(u.id());
      latitude = p.homeLatitude();
      longitude = p.homeLongitude();
    }
    m.addAttribute("reports", service.feed(u, latitude, longitude, radius, feed));
    m.addAttribute("feed", feed);
    m.addAttribute("areaLatitude", latitude);
    m.addAttribute("areaLongitude", longitude);
    m.addAttribute("areaRadius", radius);
    m.addAttribute("hasArea", latitude != null);
    m.addAttribute("isCitizen", u.role() == Role.CITIZEN);
    m.addAttribute("zones", places.zones());
    return "insights/community";
  }

  @GetMapping("/community/{id}")
  public String summary(@PathVariable long id, @AuthenticationPrincipal CurrentUser u, Model m) {
    var summary = service.summary(id, u);
    m.addAttribute("summary", summary);
    m.addAttribute("updates", service.publicUpdates(id));
    return "insights/summary";
  }

  @GetMapping("/community/nearby")
  @ResponseBody
  public List<CommunityReport> nearby(
      @RequestParam long categoryId,
      @RequestParam double latitude,
      @RequestParam double longitude,
      @AuthenticationPrincipal CurrentUser u) {
    return service.nearby(categoryId, latitude, longitude, u);
  }

  @PostMapping("/citizen/community/{id}/support")
  public String support(@PathVariable long id, @AuthenticationPrincipal CurrentUser u) {
    service.support(id, u);
    return "redirect:/community?supported";
  }

  @GetMapping("/insights")
  public String insights(@AuthenticationPrincipal CurrentUser u, Model m) {
    m.addAttribute("totals", service.totals(u));
    m.addAttribute("categoryTotals", service.categories(u));
    m.addAttribute("averageHours", service.averageHours(u));
    m.addAttribute("categoryRows", service.categoryRows(u));
    m.addAttribute("zoneRows", service.zoneRows(u));
    m.addAttribute("overdueCount", service.overdueCount(u));
    if (u.role() == Role.ADMIN) m.addAttribute("departmentLoad", service.departmentLoad());
    m.addAttribute(
        "scopeLabel",
        u.role() == Role.ADMIN
            ? "All community reports"
            : u.role() == Role.OFFICER ? "Your assigned reports" : "Your own reports");
    return "insights/dashboard";
  }

  @GetMapping("/insights/export.csv")
  @ResponseBody
  public ResponseEntity<byte[]> csv(
      @AuthenticationPrincipal CurrentUser u,
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String q) {
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
        .cacheControl(CacheControl.noStore())
        .header("Content-Disposition", "attachment; filename=civicpulse-reports.csv")
        .body(service.csv(u, status, q).getBytes(StandardCharsets.UTF_8));
  }
}
