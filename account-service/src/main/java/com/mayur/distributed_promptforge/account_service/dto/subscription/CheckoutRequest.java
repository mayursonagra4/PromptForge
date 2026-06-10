package com.mayur.distributed_promptforge.account_service.dto.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutRequest(
        @NotNull @Positive Long planId
) {
}
