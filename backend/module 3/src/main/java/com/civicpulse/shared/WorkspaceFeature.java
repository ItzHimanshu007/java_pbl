package com.civicpulse.shared;

import com.civicpulse.auth.CurrentUser;
import org.springframework.ui.Model;

/** Optional module hook: the base runs before any feature is imported. */
public interface WorkspaceFeature {
  Role role();

  String render(CurrentUser user, Model model);
}
