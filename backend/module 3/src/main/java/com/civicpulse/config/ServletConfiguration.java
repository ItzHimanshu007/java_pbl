package com.civicpulse.config;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServletConfiguration {
  @Bean
  ServletRegistrationBean<HttpServlet> healthServlet() {
    HttpServlet servlet =
        new HttpServlet() {
          @Override
          protected void doGet(HttpServletRequest request, HttpServletResponse response)
              throws IOException {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":\"UP\",\"application\":\"CivicPulse\"}");
          }
        };
    return new ServletRegistrationBean<>(servlet, "/health");
  }
}
