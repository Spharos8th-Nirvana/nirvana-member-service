package com.nirvana.member.profile.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Constraint(validatedBy = IsoCountryCode.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsoCountryCode {
    String FORMAT = "[a-zA-Z]{2}";

    String message() default "국가 코드를 확인해주세요.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<IsoCountryCode, String> {
        private static final Set<String> COUNTRY_CODES = Set.of(Locale.getISOCountries());
        private static final Pattern FORMAT_PATTERN = Pattern.compile(FORMAT);

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            // ProfileRequest's @Pattern reports malformed inputs as VALIDATION_FAILED.
            return value == null || !FORMAT_PATTERN.matcher(value).matches()
                    || COUNTRY_CODES.contains(value.toUpperCase(Locale.ROOT));
        }
    }
}
