package com.company.chatplatform.common.core.exception;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", 403);
    }

    public ForbiddenException(String message, String errorCode) {
        super(message, errorCode, 403);
    }
}
