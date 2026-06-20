package com.mayur.distributed_promptforge.workspace_service.dto.project;

import jakarta.validation.constraints.NotBlank;

public record SaveFileRequest(
        @NotBlank(message = "Path is required")
        String path,

        String content
) {
}
