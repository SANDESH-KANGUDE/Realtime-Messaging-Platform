package com.company.chatplatform.common.core.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, "RESOURCE_CONFLICT", 409);
    }

    public ConflictException(String message, String errorCode) {
        super(message, errorCode, 409);
    }
}
