package com.mayur.distributed_promptforge.common_lib.event;

public record FileStoreRequestEvent(
        Long projectId,
        String sagaId,
        String filePath,
        String content,
        Long userId
) {}