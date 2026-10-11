package com.civicpulse.shared;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class FeatureErrorAdvice {
  @ExceptionHandler(FeatureException.class)
  public String feature(FeatureException ex, HttpServletResponse response, Model model) {
    response.setStatus(ex.status());
    model.addAttribute("errorCode", ex.status());
    model.addAttribute(
        "errorTitle",
        ex.status() == 409 ? "This report has changed" : "We could not complete that action");
    model.addAttribute("errorMessage", ex.getMessage());
    return "error";
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String oversized(HttpServletResponse response, Model model) {
    return feature(
        new FeatureException(413, "Choose an image smaller than 5 MB."), response, model);
  }
}
