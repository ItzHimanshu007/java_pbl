package com.civicpulse.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Serves the interface dictionaries to browser scripts from the same JSON files the JSP pages
 * use, so client and server text cannot drift apart.
 */
@RestController
public class TranslationScriptController {
  private final byte[] script;

  public TranslationScriptController(UiText text, ObjectMapper json) throws JsonProcessingException {
    script =
        ("'use strict';\nwindow.CivicHindi="
                + json.writeValueAsString(text.hindi())
                + ";\nwindow.CivicEnglish="
                + json.writeValueAsString(text.english())
                + ";\n")
            .getBytes(StandardCharsets.UTF_8);
  }

  @GetMapping("/i18n/messages.js")
  public ResponseEntity<byte[]> messages() {
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "javascript", StandardCharsets.UTF_8))
        .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
        .body(script);
  }
}
