package com.civicpulse.insights;

import com.civicpulse.shared.*;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class InsightsNavigation implements NavigationContributor {
  public List<String> styles() {
    return List.of();
  }

  public List<String> scripts() {
    return List.of();
  }

  public List<NavigationLink> links(Role role) {
    return List.of(
        new NavigationLink("Community map", "/community"),
        new NavigationLink("Insights", "/insights"));
  }
}
