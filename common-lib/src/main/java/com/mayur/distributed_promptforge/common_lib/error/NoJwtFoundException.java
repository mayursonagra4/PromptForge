package com.mayur.distributed_promptforge.common_lib.error;

import org.springframework.security.core.AuthenticationException;

public class NoJwtFoundException extends AuthenticationException {
    public NoJwtFoundException(String msg) {
        super(msg);
    }
}
