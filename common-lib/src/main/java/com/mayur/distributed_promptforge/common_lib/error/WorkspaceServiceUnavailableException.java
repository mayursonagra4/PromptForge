package com.mayur.distributed_promptforge.common_lib.error;

public class WorkspaceServiceUnavailableException extends RuntimeException {
    public WorkspaceServiceUnavailableException(String message) {
        super(message);
    }

    public WorkspaceServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
