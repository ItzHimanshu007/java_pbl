package com.civicpulse.auth;

import com.civicpulse.shared.Role;

/** Safe authenticated principal. Never contains a password hash. */
public record CurrentUser(long id, String displayName, String email, Role role, String tokenId) {}
