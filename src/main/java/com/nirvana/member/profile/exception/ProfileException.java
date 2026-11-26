package com.nirvana.member.profile.exception;

public class ProfileException extends RuntimeException {
    private final ErrorCode errorCode;

    public ProfileException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
