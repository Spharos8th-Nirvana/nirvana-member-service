package com.nirvana.member.profile.repository;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.dao.DataIntegrityViolationException;

public enum ProfileUniqueConstraint {
    USER_UUID,
    ACTIVE_NICKNAME,
    UNKNOWN;

    public static ProfileUniqueConstraint from(DataIntegrityViolationException exception) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation
                    && violation.getKind() == ConstraintKind.UNIQUE) {
                return fromName(violation.getConstraintName());
            }
        }
        return UNKNOWN;
    }

    private static ProfileUniqueConstraint fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        String key = name.substring(name.lastIndexOf('.') + 1);
        return switch (key) {
            case "UK_userProfiles_userUuid" -> USER_UUID;
            case "UK_userProfiles_nicknameActive" -> ACTIVE_NICKNAME;
            default -> UNKNOWN;
        };
    }
}
