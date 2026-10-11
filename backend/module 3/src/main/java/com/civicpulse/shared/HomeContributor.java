package com.civicpulse.shared;

import org.springframework.ui.Model;

/** Optional module hook that adds public, non-personal content to the home page. */
public interface HomeContributor {
  void add(Model model);
}
