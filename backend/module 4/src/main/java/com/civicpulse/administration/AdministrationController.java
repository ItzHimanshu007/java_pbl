package com.civicpulse.administration;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdministrationController {
  private final AdministrationService service;
  private final ComplaintDao dao;

  public AdministrationController(AdministrationService service, ComplaintDao dao) {
    this.service = service;
    this.dao = dao;
  }

  @GetMapping("/admin/reports")
  public String reports(
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(required = false) Long category,
      @RequestParam(defaultValue = "0") int page,
      Model m) {
    int p = ComplaintController.page(page);
    m.addAttribute("reports", dao.list("all", null, status, q, category, p, 10));
    m.addAttribute("total", dao.count("all", null, status, q, category));
    m.addAttribute("categories", dao.categories());
    m.addAttribute("categoryFilter", category);
    m.addAttribute("page", p);
    m.addAttribute("statusFilter", status);
    m.addAttribute("searchQuery", q);
    m.addAttribute("statuses", ComplaintStatus.values());
    return "administration/reports";
  }

  @GetMapping("/admin/officers")
  public String officers(Model m) {
    m.addAttribute("officerForm", new OfficerForm());
    return officerPage(m);
  }

  @PostMapping("/admin/officers")
  public String create(
      @Valid @ModelAttribute OfficerForm officerForm,
      BindingResult errors,
      @AuthenticationPrincipal CurrentUser user,
      Model m) {
    if (!errors.hasErrors())
      try {
        service.createOfficer(officerForm, user);
        return "redirect:/admin/officers?created";
      } catch (DuplicateKeyException ex) {
        errors.rejectValue("email", "duplicate", "This email is already registered.");
      } catch (IllegalArgumentException ex) {
        errors.reject("password", ex.getMessage());
      } catch (FeatureException ex) {
        if (ex.status() != 400) throw ex;
        errors.reject("invalid", ex.getMessage());
      }
    return officerPage(m);
  }

  @PostMapping("/admin/officers/{id}/access")
  public String access(
      @PathVariable long id,
      @RequestParam boolean enabled,
      @AuthenticationPrincipal CurrentUser user) {
    service.setEnabled(id, enabled, user);
    return "redirect:/admin/officers?updated";
  }

  @PostMapping("/admin/reports/{id}/assign")
  public String assign(
      @PathVariable long id,
      @RequestParam long officerId,
      @RequestParam long departmentId,
      @RequestParam String note,
      @AuthenticationPrincipal CurrentUser user) {
    service.assign(id, officerId, departmentId, note, user);
    return "redirect:/reports/" + id + "?assigned";
  }

  @PostMapping("/admin/reports/{id}/reject")
  public String reject(
      @PathVariable long id, @RequestParam String note, @AuthenticationPrincipal CurrentUser user) {
    service.reject(id, note, user);
    return "redirect:/reports/" + id + "?rejected";
  }

  private String officerPage(Model m) {
    m.addAttribute("officers", service.officers());
    m.addAttribute("departments", service.departments());
    return "administration/officers";
  }
}
