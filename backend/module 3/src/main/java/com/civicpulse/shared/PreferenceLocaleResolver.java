package com.civicpulse.shared;

import com.civicpulse.account.ProfileDao;
import com.civicpulse.auth.CurrentUser;
import jakarta.servlet.http.*;
import java.util.Locale;
import org.springframework.context.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
public class PreferenceLocaleResolver {
  @Bean
  public LocaleResolver localeResolver(ProfileDao profiles) {
    return new LocaleResolver() {
      public Locale resolveLocale(HttpServletRequest request) {
        var principal = SecurityContextHolder.getContext().getAuthentication();
        if (principal != null && principal.getPrincipal() instanceof CurrentUser user)
          return Locale.forLanguageTag(profiles.find(user.id()).language());
        if (request.getCookies() != null)
          for (var c : request.getCookies())
            if (c.getName().equals("civic_language") && c.getValue().equals("hi"))
              return Locale.forLanguageTag("hi");
        return Locale.ENGLISH;
      }

      public void setLocale(HttpServletRequest r, HttpServletResponse s, Locale l) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
