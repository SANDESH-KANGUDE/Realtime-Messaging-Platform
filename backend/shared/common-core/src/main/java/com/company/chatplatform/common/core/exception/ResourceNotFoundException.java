package com.company.chatplatform.common.core.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }

    public ResourceNotFoundException(String message, String errorCode) {
        super(message, errorCode, 404);
    }
}
