package com.mayur.distributed_promptforge.common_lib.error;

import org.springframework.security.access.AccessDeniedException;

public class SubscriptionAccessDeniedException extends AccessDeniedException {
    public SubscriptionAccessDeniedException(String msg) {
        super(msg);
    }
}
