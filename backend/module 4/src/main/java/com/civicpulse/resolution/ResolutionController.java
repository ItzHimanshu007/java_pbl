package com.civicpulse.resolution;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.complaints.*;
import com.civicpulse.shared.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ResolutionController {
  private final ResolutionService service;
  private final ComplaintDao dao;

  public ResolutionController(ResolutionService service, ComplaintDao dao) {
    this.service = service;
    this.dao = dao;
  }

  @GetMapping("/officer/reports")
  public String work(
      @AuthenticationPrincipal CurrentUser user,
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "0") int page,
      Model m) {
    int p = ComplaintController.page(page);
    m.addAttribute("reports", dao.list("assigned", user.id(), status, q, p, 10));
    m.addAttribute("total", dao.count("assigned", user.id(), status, q));
    m.addAttribute("page", p);
    m.addAttribute("statusFilter", status);
    m.addAttribute("searchQuery", q);
    m.addAttribute("statuses", ComplaintStatus.values());
    return "resolution/work";
  }

  @PostMapping("/officer/reports/{id}/start")
  public String start(
      @PathVariable long id, @RequestParam String note, @AuthenticationPrincipal CurrentUser u) {
    service.start(id, note, u);
    return back(id);
  }

  @PostMapping("/officer/reports/{id}/progress")
  public String progress(
      @PathVariable long id, @RequestParam String note, @AuthenticationPrincipal CurrentUser u) {
    service.progress(id, note, u);
    return back(id);
  }

  @PostMapping("/officer/reports/{id}/resolve")
  public String resolve(
      @PathVariable long id,
      @RequestParam String note,
      @RequestParam(required = false) MultipartFile photo,
      @AuthenticationPrincipal CurrentUser u) {
    service.resolve(id, note, photo, u);
    return back(id);
  }

  @PostMapping("/citizen/reports/{id}/confirm")
  public String confirm(@PathVariable long id, @AuthenticationPrincipal CurrentUser u) {
    service.confirm(id, u);
    return back(id);
  }

  @PostMapping("/citizen/reports/{id}/reopen")
  public String reopen(
      @PathVariable long id, @RequestParam String note, @AuthenticationPrincipal CurrentUser u) {
    service.reopen(id, note, u);
    return back(id);
  }

  private String back(long id) {
    return "redirect:/reports/" + id + "?updated";
  }
}
