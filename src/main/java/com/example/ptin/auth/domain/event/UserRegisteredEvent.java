package com.example.ptin.auth.domain.event;

import com.example.ptin.shared.identity.UserId;

public record UserRegisteredEvent(UserId userId) {
}
