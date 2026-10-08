package com.example.ptin.ptin.domain.event;

import com.example.ptin.ptin.domain.model.PtinApplicationId;
import com.example.ptin.shared.identity.UserId;

public record PtinApplicationApprovedEvent(PtinApplicationId applicationId, UserId approvedBy) {
}
