package com.mayur.distributed_promptforge.common_lib.error;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String resourceId;

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + " with id " + resourceId + " not found");
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
}

