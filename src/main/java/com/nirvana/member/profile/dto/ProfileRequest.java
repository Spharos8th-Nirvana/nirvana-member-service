package com.nirvana.member.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record ProfileRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 30) String nickname,
        @Pattern(regexp = IsoCountryCode.FORMAT) @IsoCountryCode String countryCode
) {
    public String normalizedCountryCode() {
        return countryCode == null ? null : countryCode.toUpperCase(Locale.ROOT);
    }
}
