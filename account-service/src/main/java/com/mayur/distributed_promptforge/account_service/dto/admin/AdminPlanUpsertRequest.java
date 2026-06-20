package com.mayur.distributed_promptforge.account_service.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminPlanUpsertRequest(
        @NotBlank(message = "Plan name is required")
        String name,

        @NotNull(message = "Plan price is required")
        @Min(value = 0, message = "Plan price must be zero or greater")
        Long priceInPaise,

        @Min(value = 0, message = "Max projects cannot be negative")
        Integer maxProjects,

        @Min(value = 0, message = "Max AI tokens per day cannot be negative")
        Integer maxTokensPerDay,

        Boolean unlimitedAi,

        @NotNull(message = "Plan validity days is required")
        @Min(value = 1, message = "Plan validity days must be greater than zero")
        Integer validityDays,

        Boolean active
) {
}

