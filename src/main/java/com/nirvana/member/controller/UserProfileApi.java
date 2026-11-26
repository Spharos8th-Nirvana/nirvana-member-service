package com.nirvana.member.controller;

import com.nirvana.member.dto.request.ProfileRequest;
import com.nirvana.member.dto.response.ApiResponse;
import com.nirvana.member.dto.response.ProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;

interface UserProfileApi {
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
    ApiResponse<ProfileResponse> create(
            @Parameter(description = "Auth와 Member가 공유하는 서비스 토큰", required = true)
            String credential,
            @Parameter(description = "Auth 일반 회원 UUID")
            UUID userUuid,
            ProfileRequest request);

    record ErrorResponse(boolean success, Void data, ApiResponse.ApiError error) {
    }
}
