package com.civicpulse.insights;

import com.civicpulse.complaints.ComplaintDao;
import com.civicpulse.location.JaipurPlaces;
import com.civicpulse.shared.HomeContributor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/** City-wide figures, the service catalogue and zones for the public home page. */
@Component
@Order(40)
public class PublicHome implements HomeContributor {
  private final InsightsService insights;
  private final ComplaintDao dao;
  private final JaipurPlaces places;

  public PublicHome(InsightsService insights, ComplaintDao dao, JaipurPlaces places) {
    this.insights = insights;
    this.dao = dao;
    this.places = places;
  }

  public void add(Model model) {
    model.addAttribute("publicStats", insights.publicStats());
    model.addAttribute("serviceCategories", dao.categories());
    model.addAttribute("zones", places.zones());
  }
}
