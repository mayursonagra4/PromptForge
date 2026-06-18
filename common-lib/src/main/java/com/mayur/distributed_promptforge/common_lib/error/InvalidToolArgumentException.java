package com.mayur.distributed_promptforge.common_lib.error;

public class InvalidToolArgumentException extends RuntimeException {
    public InvalidToolArgumentException(String message) {
        super(message);
    }

    public InvalidToolArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
