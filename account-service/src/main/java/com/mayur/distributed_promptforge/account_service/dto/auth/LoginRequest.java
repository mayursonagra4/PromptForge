package com.mayur.distributed_promptforge.account_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Username/Email is required")
        @Email(message = "Invalid email format")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 50, message = "Password must be between {min} and {max} characters")
        String password
) {
}
