package com.mayur.distributed_promptforge.common_lib.event;

public record UserDeletionResponseEvent(
        Long userId,
        boolean success,
        String errorMessage
) {}
