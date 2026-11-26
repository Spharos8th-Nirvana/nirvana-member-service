package com.nirvana.member.dto.response;

import com.nirvana.member.domain.UserProfile;
import java.util.UUID;

public record ProfileResponse(UUID userUuid, String name, String nickname, String countryCode) {
    public static ProfileResponse from(UserProfile profile) {
        return new ProfileResponse(profile.getUserUuid(), profile.getName(), profile.getNickname(),
                profile.getCountryCode());
    }
}
