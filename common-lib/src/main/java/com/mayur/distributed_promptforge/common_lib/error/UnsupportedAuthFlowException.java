package com.mayur.distributed_promptforge.common_lib.error;

public class UnsupportedAuthFlowException extends RuntimeException {
    public UnsupportedAuthFlowException(String message) {
        super(message);
    }

    public UnsupportedAuthFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
