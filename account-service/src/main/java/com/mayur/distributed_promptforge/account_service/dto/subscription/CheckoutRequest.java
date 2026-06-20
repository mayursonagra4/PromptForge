package com.mayur.distributed_promptforge.account_service.dto.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutRequest(
        @NotNull(message = "Plan ID is required")
        @Positive(message = "Plan ID must be positive")
        Long planId
) {
}
