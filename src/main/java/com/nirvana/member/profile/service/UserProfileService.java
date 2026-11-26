package com.nirvana.member.profile.service;

import com.nirvana.member.profile.domain.UserProfile;
import com.nirvana.member.profile.exception.ErrorCode;
import com.nirvana.member.profile.exception.ProfileException;
import com.nirvana.member.profile.repository.ProfileUniqueConstraint;
import com.nirvana.member.profile.repository.UserProfileRepository;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class UserProfileService {
    private final UserProfileRepository profiles;
    private final TransactionTemplate transaction;

    public UserProfileService(UserProfileRepository profiles, PlatformTransactionManager transactionManager) {
        this.profiles = profiles;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public UserProfile create(UUID userUuid, String name, String nickname, String countryCode) {
        String uuid = userUuid.toString();
        try {
            return transaction.execute(status -> {
                if (profiles.existsByUserUuid(uuid)) {
                    throw new ProfileException(ErrorCode.PROFILE_ALREADY_EXISTS);
                }
                return profiles.saveAndFlush(new UserProfile(userUuid, name.strip(), nickname.strip(), countryCode));
            });
        } catch (DataIntegrityViolationException exception) {
            ProfileUniqueConstraint conflict = ProfileUniqueConstraint.from(exception);
            if (conflict == ProfileUniqueConstraint.USER_UUID) {
                throw new ProfileException(ErrorCode.PROFILE_ALREADY_EXISTS);
            }
            if (conflict == ProfileUniqueConstraint.ACTIVE_NICKNAME) {
                // Recheck with a fresh snapshot after the failed insert has rolled back.
                boolean profileExists = Boolean.TRUE.equals(
                        transaction.execute(status -> profiles.existsByUserUuid(uuid)));
                throw new ProfileException(profileExists ? ErrorCode.PROFILE_ALREADY_EXISTS : ErrorCode.NICKNAME_TAKEN);
            }
            throw exception;
        }
    }
}
