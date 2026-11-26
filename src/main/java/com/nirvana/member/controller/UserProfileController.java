package com.nirvana.member.controller;

import com.nirvana.member.dto.request.ProfileRequest;
import com.nirvana.member.dto.response.ApiResponse;
import com.nirvana.member.dto.response.ProfileResponse;
import com.nirvana.member.service.UserProfileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class UserProfileController implements UserProfileApi {
    private final UserProfileService profiles;
    private final ProfileAccess access;

    @Override
    public ApiResponse<ProfileResponse> create(String credential, UUID userUuid, ProfileRequest request) {
        access.requireAuth(credential);
        return ApiResponse.ok(ProfileResponse.from(profiles.create(
                userUuid, request.name(), request.nickname(), request.normalizedCountryCode())));
    }
}
