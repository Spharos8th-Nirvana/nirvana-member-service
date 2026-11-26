package com.nirvana.member.controller;

import com.nirvana.member.dto.request.ProfileRequest;
import com.nirvana.member.dto.response.ApiResponse;
import com.nirvana.member.dto.response.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;

interface UserProfileApi {
    String SERVICE_CREDENTIAL = "X-Member-Service-Token";

    @Operation(summary = "일반 회원 프로필 생성", description = "Auth가 회원가입 중 호출하는 내부 API입니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                description = "프로필 생성 완료", useReturnTypeSchema = true),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                description = "VALIDATION_FAILED 또는 INVALID_COUNTRY_CODE",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                description = "AUTHENTICATION_REQUIRED",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                description = "PROFILE_ALREADY_EXISTS 또는 NICKNAME_TAKEN",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500",
                description = "INTERNAL_ERROR",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = "/internal/profiles/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<ProfileResponse> create(
            @Parameter(description = "Auth와 Member가 공유하는 서비스 토큰", required = true)
            @RequestHeader(name = SERVICE_CREDENTIAL, required = false) String credential,
            @Parameter(description = "Auth 일반 회원 UUID")
            @PathVariable("userUuid") UUID userUuid,
            @Valid @RequestBody ProfileRequest request);

    record ErrorResponse(boolean success, Void data, ApiResponse.ApiError error) {
    }
}
