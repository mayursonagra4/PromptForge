package com.mayur.distributed_promptforge.common_lib.error;

public class TokenLimitExceededException extends RuntimeException {
    public TokenLimitExceededException(String message) {
        super(message);
    }

    public TokenLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
