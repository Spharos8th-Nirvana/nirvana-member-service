package com.nirvana.member.profile.repository;

import com.nirvana.member.profile.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByUserUuid(String userUuid);
}
