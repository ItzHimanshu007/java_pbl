package com.civicpulse.complaints;

import com.civicpulse.auth.CurrentUser;
import org.springframework.ui.Model;

/** Optional feature hook for private detail actions. */
public interface DetailContributor {
  void add(Complaint complaint, CurrentUser user, Model model);
}
