package com.mayur.distributed_promptforge.common_lib.error;

import org.springframework.security.core.AuthenticationException;

public class UserBlockedException extends AuthenticationException {
    public UserBlockedException(String msg) {
        super(msg);
    }

    public UserBlockedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
