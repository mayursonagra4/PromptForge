package com.mayur.distributed_promptforge.intelligence_service.client.dto;

public record SaveFileRequest(
        String path,
        String content
) {
}
