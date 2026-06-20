package com.mayur.distributed_promptforge.workspace_service.dto.member;

import com.mayur.distributed_promptforge.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotBlank(message = "Username/Email is required")
        @Email(message = "Invalid email format")
        String username,

        @NotNull(message = "Project role is required")
        ProjectRole role
) {
}
