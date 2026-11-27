package com.nirvana.member.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nirvana.member.dto.request.IsoCountryCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileCreationRequested(
        @NotNull UUID eventId,
        @NotNull UUID userUuid,
        @NotBlank @Size(max = 100) String name,
        @Pattern(regexp = IsoCountryCode.FORMAT) @IsoCountryCode String countryCode
) {
    public String normalizedCountryCode() {
        return countryCode == null ? null : countryCode.toUpperCase(Locale.ROOT);
    }
}
