package com.civicpulse.config;

import com.civicpulse.auth.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  CookieCsrfTokenRepository csrfRepository(JwtSettings settings) {
    var repository = new CookieCsrfTokenRepository();
    repository.setCookieName("civic_csrf");
    repository.setCookieCustomizer(
        cookie -> cookie.httpOnly(true).secure(settings.secureCookie()).sameSite("Lax").path("/"));
    return repository;
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http, JwtAuthenticationFilter jwt, CookieCsrfTokenRepository csrf)
      throws Exception {
    http.authorizeHttpRequests(
            routes ->
                routes
                    .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/assets/**",
                        "/error",
                        "/health",
                        "/track",
                        "/i18n/**",
                        "/preferences")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/officer/**")
                    .hasRole("OFFICER")
                    .requestMatchers("/citizen/**")
                    .hasRole("CITIZEN")
                    .anyRequest()
                    .authenticated())
        .csrf(
            config ->
                config
                    .csrfTokenRepository(csrf)
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                    // JWT is authenticated on every request, including assets. Rotate CSRF only at
                    // login/logout.
                    .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
        .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .requestCache(config -> config.requestCache(new NullRequestCache()))
        .formLogin(config -> config.disable())
        .httpBasic(config -> config.disable())
        .logout(config -> config.disable())
        .exceptionHandling(
            config ->
                config
                    .authenticationEntryPoint(
                        (request, response, error) ->
                            response.sendRedirect(request.getContextPath() + "/login?expired"))
                    .accessDeniedHandler((request, response, error) -> response.sendError(403)))
        .headers(
            config ->
                config.contentSecurityPolicy(
                    policy ->
                        policy.policyDirectives(
                            "default-src 'self'; script-src 'self'; style-src 'self'; img-src"
                                + " 'self' data: blob: https://tile.openstreetmap.org"
                                + " https://server.arcgisonline.com;"
                                + " frame-ancestors 'none'; base-uri 'self'; form-action 'self'")))
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  FilterRegistrationBean<JwtAuthenticationFilter> disableDuplicateFilterRegistration(
      JwtAuthenticationFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
