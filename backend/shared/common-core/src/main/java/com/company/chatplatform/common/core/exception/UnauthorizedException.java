package com.company.chatplatform.common.core.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", 401);
    }

    public UnauthorizedException(String message, String errorCode) {
        super(message, errorCode, 401);
    }
}
