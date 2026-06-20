package com.mayur.distributed_promptforge.account_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Reset code is required")
        String code,

        @NotBlank(message = "New password is required")
        @Size(min = 4, message = "Password must be at least {min} characters long")
        String newPassword
) {
}
