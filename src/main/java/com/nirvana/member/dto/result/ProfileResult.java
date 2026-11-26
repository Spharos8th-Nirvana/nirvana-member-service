package com.nirvana.member.dto.result;

import java.util.UUID;

public record ProfileResult(UUID userUuid, String name, String nickname, String countryCode) {
}
