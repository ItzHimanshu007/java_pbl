package com.civicpulse.shared;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class UiText {
  private final Map<String, String> hindi;
  private final Map<String, String> english;

  public UiText(ObjectMapper mapper) throws java.io.IOException {
    try (var in = new ClassPathResource("i18n/hi.json").getInputStream()) {
      hindi = mapper.readValue(in, new TypeReference<Map<String, String>>() {});
    }
    try (var in = new ClassPathResource("i18n/en.json").getInputStream()) {
      english = mapper.readValue(in, new TypeReference<Map<String, String>>() {});
    }
  }

  public Map<String, String> hindi() {
    return Collections.unmodifiableMap(hindi);
  }

  public Map<String, String> english() {
    return Collections.unmodifiableMap(english);
  }

  public Map<String, String> forLanguage(String language) {
    return new AbstractMap<>() {
      public String get(Object key) {
        String v = String.valueOf(key);
        return language.equals("hi") ? hindi.getOrDefault(v, v) : english.getOrDefault(v, v);
      }

      public Set<Entry<String, String>> entrySet() {
        return hindi.entrySet();
      }
    };
  }
}
