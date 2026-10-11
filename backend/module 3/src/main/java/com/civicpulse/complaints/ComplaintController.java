package com.civicpulse.complaints;

import com.civicpulse.auth.CurrentUser;
import com.civicpulse.location.JaipurPlaces;
import com.civicpulse.shared.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ComplaintController {
  private final ComplaintDao dao;
  private final ComplaintAccess access;
  private final SubmissionService service;
  private final ImageStore images;
  private final ObjectProvider<DetailContributor> details;
  private final JaipurPlaces places;

  public ComplaintController(
      ComplaintDao dao,
      ComplaintAccess access,
      SubmissionService service,
      ImageStore images,
      ObjectProvider<DetailContributor> details,
      JaipurPlaces places) {
    this.dao = dao;
    this.access = access;
    this.service = service;
    this.images = images;
    this.details = details;
    this.places = places;
  }

  @GetMapping("/citizen/reports/new")
  public String create(@RequestParam(required = false) Long category, Model m) {
    var form = new ComplaintForm();
    if (category != null && dao.categories().stream().anyMatch(c -> c.id() == category))
      form.setCategoryId(category);
    m.addAttribute("complaintForm", form);
    return form(m, null);
  }

  @PostMapping("/citizen/reports")
  public String submit(
      @Valid @ModelAttribute ComplaintForm complaintForm,
      BindingResult errors,
      @RequestParam(required = false) MultipartFile photo,
      @AuthenticationPrincipal CurrentUser user,
      Model m) {
    if (!errors.hasErrors())
      try {
        return "redirect:/reports/" + service.submit(complaintForm, photo, user) + "?created";
      } catch (FeatureException ex) {
        if (ex.status() != 400) throw ex;
        errors.reject("invalid", ex.getMessage());
      }
    return form(m, null);
  }

  @GetMapping("/citizen/reports")
  public String mine(
      @AuthenticationPrincipal CurrentUser user,
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "false") boolean trash,
      Model m) {
    int p = page(page);
    m.addAttribute("trash", trash);
    m.addAttribute("totals", dao.totals("mine", user.id()));
    m.addAttribute("reports", dao.list(trash ? "trash" : "mine", user.id(), status, q, p, 10));
    m.addAttribute("total", dao.count(trash ? "trash" : "mine", user.id(), status, q));
    m.addAttribute("page", p);
    m.addAttribute("statusFilter", status);
    m.addAttribute("searchQuery", q);
    m.addAttribute("statuses", ComplaintStatus.values());
    return "complaints/list";
  }

  @GetMapping("/reports/{id}")
  public String detail(@PathVariable long id, @AuthenticationPrincipal CurrentUser user, Model m) {
    var c = dao.find(id);
    access.read(c, user);
    m.addAttribute("report", c);
    m.addAttribute("events", dao.events(id));
    var all = images.list(id);
    m.addAttribute("originals", all.stream().filter(i -> i.purpose().equals("ORIGINAL")).toList());
    m.addAttribute("repairs", all.stream().filter(i -> i.purpose().equals("REPAIR")).toList());
    m.addAttribute("canDelete", access.isOwner(c, user));
    m.addAttribute("isOwner", access.isOwner(c, user));
    m.addAttribute("zone", places.zoneOf(c.latitude(), c.longitude()));
    details.orderedStream().forEach(d -> d.add(c, user, m));
    return "complaints/detail";
  }

  @RequestMapping(
      value = "/citizen/reports/{id}/edit",
      method = {RequestMethod.GET, RequestMethod.POST})
  public String editingRemoved(@PathVariable long id, @AuthenticationPrincipal CurrentUser user) {
    access.owner(dao.find(id), user);
    throw FeatureException.conflict(
        "Submitted reports cannot be edited. Delete the report and submit a new one.");
  }

  @PostMapping("/citizen/reports/{id}/delete")
  public String delete(@PathVariable long id, @AuthenticationPrincipal CurrentUser user) {
    service.delete(id, user);
    return "redirect:/citizen/reports?deleted";
  }

  @PostMapping("/citizen/reports/{id}/restore")
  public String restore(@PathVariable long id, @AuthenticationPrincipal CurrentUser user) {
    service.restore(id, user);
    return "redirect:/citizen/reports?restored";
  }

  @GetMapping("/reports/images/{id}")
  @ResponseBody
  public ResponseEntity<byte[]> image(
      @PathVariable long id, @AuthenticationPrincipal CurrentUser user) {
    access.read(dao.find(images.complaintFor(id)), user);
    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_PNG)
        .cacheControl(CacheControl.noStore())
        .header("Content-Disposition", "inline; filename=report-image.png")
        .body(images.bytes(id));
  }

  private String form(Model m, Long id) {
    m.addAttribute("categories", dao.categories());
    m.addAttribute("zones", places.zones());
    m.addAttribute("editId", id);
    return "complaints/form";
  }

  public static int page(int page) {
    if (page < 0 || page > 10000) throw FeatureException.invalid("Choose a valid page.");
    return page;
  }
}
