package com.civicpulse.account;

import com.civicpulse.shared.Role;
import java.time.Instant;

/** Internal persistence record; never serialize this as an API response. */
public record AccountUser(
    long id,
    String displayName,
    String email,
    String passwordHash,
    Role role,
    boolean enabled,
    Instant createdAt) {}
