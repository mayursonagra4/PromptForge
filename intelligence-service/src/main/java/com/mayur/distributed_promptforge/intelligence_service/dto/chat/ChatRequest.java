package com.mayur.distributed_promptforge.intelligence_service.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChatRequest(
        @NotBlank(message = "Message is required")
        String message,

        @NotNull(message = "Project ID is required")
        @Positive(message = "Project ID must be positive")
        Long projectId
) {}
