package com.nirvana.member.repository;

import com.nirvana.member.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByUserUuid(String userUuid);

    @Query(value = "select count(*) from userProfiles where nicknameActive = ?1", nativeQuery = true)
    long countActiveNickname(String nickname);
}
