package com.mayur.distributed_promptforge.account_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupCompleteRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Name is required")
        @Size(min = 1, max = 50, message = "Name must be between {min} and {max} characters")
        String name,

        @NotBlank(message = "Password is required")
        @Size(min = 4, message = "Password must be at least {min} characters long")
        String password
) {
}
