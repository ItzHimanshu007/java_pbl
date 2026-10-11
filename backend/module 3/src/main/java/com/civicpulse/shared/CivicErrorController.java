package com.civicpulse.shared;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CivicErrorController implements ErrorController {
  @RequestMapping("/error")
  public String error(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int code = status instanceof Integer number ? number : 500;
    model.addAttribute("errorCode", code);
    model.addAttribute(
        "errorTitle",
        code == 403
            ? "This page needs a different permission"
            : code == 404 ? "Page not found" : "Something needs our attention");
    model.addAttribute(
        "errorMessage",
        code == 403
            ? "Use the workspace assigned to your account."
            : code == 404
                ? "Check the address or return to the home page."
                : "Please try again in a moment.");
    return "error";
  }
}
