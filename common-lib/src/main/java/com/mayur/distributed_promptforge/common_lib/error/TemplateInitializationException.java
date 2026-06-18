package com.mayur.distributed_promptforge.common_lib.error;

public class TemplateInitializationException extends RuntimeException {
    public TemplateInitializationException(String message) {
        super(message);
    }

    public TemplateInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
