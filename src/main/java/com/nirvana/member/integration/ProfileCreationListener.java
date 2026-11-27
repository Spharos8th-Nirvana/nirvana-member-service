package com.nirvana.member.integration;

import com.nirvana.member.dto.event.ProfileCreationRequested;
import com.nirvana.member.service.UserProfileService;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
class ProfileCreationListener {
    private static final String EVENT_TYPE = "ProfileCreationRequested";

    private final ObjectMapper json;
    private final Validator validator;
    private final UserProfileService profiles;
    private final ProfileCreatedPublisher completed;

    @KafkaListener(topics = "${member.profile-events.topic}", groupId = "${member.profile-events.group-id}")
    void receive(ConsumerRecord<String, String> record) {
        ProfileCreationRequested request;
        try {
            if (record.value() == null) {
                return;
            }
            var payload = json.readTree(record.value());
            if (payload == null || !payload.isObject() || !payload.path("eventType").isString()) {
                throw new InvalidProfileCreationEvent();
            }
            if (!EVENT_TYPE.equals(payload.path("eventType").stringValue())) {
                return;
            }
            request = json.treeToValue(payload, ProfileCreationRequested.class);
        } catch (JacksonException exception) {
            throw new InvalidProfileCreationEvent();
        }
        if (!validator.validate(request).isEmpty()) {
            throw new InvalidProfileCreationEvent();
        }
        profiles.createFromSignup(request.userUuid(), request.name(), request.normalizedCountryCode());
        completed.publish(request.userUuid(), request.eventId());
    }

    static class InvalidProfileCreationEvent extends RuntimeException {
    }
}
