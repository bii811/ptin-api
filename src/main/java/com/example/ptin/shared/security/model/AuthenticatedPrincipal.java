package com.example.ptin.shared.security.model;

import com.example.ptin.shared.identity.UserId;

public record AuthenticatedPrincipal(UserId userId, String role) {
}
