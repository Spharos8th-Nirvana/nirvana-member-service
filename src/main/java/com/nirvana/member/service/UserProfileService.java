package com.nirvana.member.service;

import com.nirvana.member.domain.UserProfile;
import com.nirvana.member.exception.ErrorCode;
import com.nirvana.member.exception.ProfileException;
import com.nirvana.member.repository.UserProfileRepository;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class UserProfileService {
    private static final int MYSQL_DUPLICATE_KEY = 1062;
    private static final Pattern PROFILE_UNIQUE_KEY =
            Pattern.compile("[.']UK_userProfiles_userUuid'$");
    private static final Pattern NICKNAME_UNIQUE_KEY =
            Pattern.compile("[.']UK_userProfiles_nicknameActive'$");

    private final UserProfileRepository profiles;
    private final TransactionTemplate transaction;

    public UserProfileService(UserProfileRepository profiles, PlatformTransactionManager transactionManager) {
        this.profiles = profiles;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public UserProfile create(UUID userUuid, String name, String nickname, String countryCode) {
        String normalizedNickname = nickname.strip();
        try {
            return transaction.execute(status -> {
                if (profiles.existsByUserUuid(userUuid.toString())) {
                    throw new ProfileException(ErrorCode.PROFILE_ALREADY_EXISTS);
                }
                if (profiles.countActiveNickname(normalizedNickname) > 0) {
                    throw new ProfileException(ErrorCode.NICKNAME_TAKEN);
                }
                return profiles.saveAndFlush(new UserProfile(userUuid, name.strip(), normalizedNickname, countryCode));
            });
        } catch (DataIntegrityViolationException exception) {
            if (!(exception.getMostSpecificCause() instanceof SQLException sql)
                    || sql.getErrorCode() != MYSQL_DUPLICATE_KEY || sql.getMessage() == null) {
                throw exception;
            }
            if (PROFILE_UNIQUE_KEY.matcher(sql.getMessage()).find()) {
                throw new ProfileException(ErrorCode.PROFILE_ALREADY_EXISTS);
            }
            if (NICKNAME_UNIQUE_KEY.matcher(sql.getMessage()).find()) {
                // Recheck with a fresh snapshot after the failed insert has rolled back.
                boolean profileExists = Boolean.TRUE.equals(
                        transaction.execute(status -> profiles.existsByUserUuid(userUuid.toString())));
                throw new ProfileException(profileExists ? ErrorCode.PROFILE_ALREADY_EXISTS : ErrorCode.NICKNAME_TAKEN);
            }
            throw exception;
        }
    }
}
