package com.nirvana.member.controller;

import com.nirvana.member.dto.request.ProfileRequest;
import com.nirvana.member.dto.response.ApiResponse;
import com.nirvana.member.dto.response.ProfileResponse;
import com.nirvana.member.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class UserProfileController implements UserProfileApi {
    static final String SERVICE_CREDENTIAL = "X-Member-Service-Token";

    private final UserProfileService profiles;
    private final ProfileAccess access;

    @Override
    @PostMapping(path = "/internal/profiles/{userUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProfileResponse> create(
            @RequestHeader(name = SERVICE_CREDENTIAL, required = false) String credential,
            @PathVariable("userUuid") UUID userUuid,
            @Valid @RequestBody ProfileRequest request) {
        access.requireAuth(credential);
        return ApiResponse.ok(ProfileResponse.from(profiles.create(
                userUuid, request.name(), request.nickname(), request.normalizedCountryCode())));
    }
}
