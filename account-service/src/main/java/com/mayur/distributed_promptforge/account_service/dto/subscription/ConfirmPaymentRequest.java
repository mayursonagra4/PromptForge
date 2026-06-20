package com.mayur.distributed_promptforge.account_service.dto.subscription;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentRequest(
        @NotBlank(message = "Order ID is required")
        String orderId,
        @NotBlank(message = "Payment ID is required")
        String paymentId
) {
}
