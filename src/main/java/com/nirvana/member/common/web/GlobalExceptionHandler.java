package com.nirvana.member.common.web;

import com.nirvana.member.profile.exception.ErrorCode;
import com.nirvana.member.profile.exception.ProfileException;
import com.nirvana.member.profile.dto.IsoCountryCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProfileException.class)
    ResponseEntity<ApiResponse<Void>> profileError(ProfileException exception) {
        return respond(exception.errorCode());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (exception instanceof MethodArgumentNotValidException invalid) {
            var errors = invalid.getBindingResult().getFieldErrors();
            ErrorCode code = !errors.isEmpty() && errors.stream()
                    .allMatch(error -> IsoCountryCode.class.getSimpleName().equals(error.getCode()))
                    ? ErrorCode.INVALID_COUNTRY_CODE : ErrorCode.VALIDATION_FAILED;
            return super.handleExceptionInternal(exception, errorBody(code), headers, code.status(), request);
        }
        if (exception instanceof MethodArgumentTypeMismatchException
                || exception instanceof HttpMessageNotReadableException) {
            ErrorCode code = ErrorCode.VALIDATION_FAILED;
            return super.handleExceptionInternal(exception, errorBody(code), headers, code.status(), request);
        }
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpectedError(Exception exception) {
        log.error("Unexpected Member API error", exception);
        return respond(ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> respond(ErrorCode code) {
        return ResponseEntity.status(code.status()).body(errorBody(code));
    }

    private ApiResponse<Void> errorBody(ErrorCode code) {
        return ApiResponse.failure(code.code(), code.message(), MDC.get("traceId"));
    }
}
