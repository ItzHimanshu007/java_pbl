package com.civicpulse.shared;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DatabaseErrorAdvice {
  @ExceptionHandler(DataAccessException.class)
  public String databaseError(HttpServletResponse response, Model model) {
    response.setStatus(503);
    model.addAttribute("errorCode", 503);
    model.addAttribute("errorTitle", "Service temporarily unavailable");
    model.addAttribute(
        "errorMessage", "We could not save or load your information. Please try again shortly.");
    return "error";
  }
}
