package com.mayur.distributed_promptforge.common_lib.error;

public class PermissionCheckException extends RuntimeException {
    public PermissionCheckException(String message) {
        super(message);
    }

    public PermissionCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
