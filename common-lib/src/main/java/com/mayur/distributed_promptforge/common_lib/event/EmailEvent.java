package com.mayur.distributed_promptforge.common_lib.event;

public record EmailEvent(
        String to,
        String subject,
        String body
) {}
