package com.nirvana.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "userProfiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userUuid", nullable = false, updatable = false, length = 36)
    private String userUuid;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(name = "countryCode", length = 2)
    private String countryCode;

    protected UserProfile() {
    }

    public UserProfile(UUID userUuid, String name, String nickname, String countryCode) {
        this.userUuid = userUuid.toString();
        this.name = name;
        this.nickname = nickname;
        this.countryCode = countryCode;
    }

    public UUID getUserUuid() {
        return UUID.fromString(userUuid);
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
