package com.civicpulse.shared;

import java.util.List;

public interface NavigationContributor {
  List<NavigationLink> links(Role role);

  default List<String> styles() {
    return List.of();
  }

  default List<String> scripts() {
    return List.of();
  }
}
