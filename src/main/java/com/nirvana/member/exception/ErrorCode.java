package com.nirvana.member.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 등록된 프로필입니다."),
    NICKNAME_TAKEN(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    INVALID_COUNTRY_CODE(HttpStatus.BAD_REQUEST, "국가 코드를 확인해주세요."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값을 확인해주세요."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증 정보를 확인해주세요."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return name();
    }

    public String message() {
        return message;
    }
}
