package com.nirvana.member.controller;

import com.nirvana.member.exception.ErrorCode;
import com.nirvana.member.exception.ProfileException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ProfileAccess {
    private final String authSecret;

    ProfileAccess(@Value("${member.access.auth-secret:}") String authSecret) {
        this.authSecret = authSecret;
    }

    void requireAuth(String credential) {
        if (authSecret.isBlank() || credential == null
                || !MessageDigest.isEqual(authSecret.getBytes(StandardCharsets.UTF_8),
                        credential.getBytes(StandardCharsets.UTF_8))) {
            throw new ProfileException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }
}
