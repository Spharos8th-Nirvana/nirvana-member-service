package com.nirvana.member.integration;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
class ProfileCreatedPublisher {
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;
    private final String topic;

    ProfileCreatedPublisher(KafkaTemplate<String, String> kafka, ObjectMapper json,
                            @Value("${member.profile-events.completed-topic}") String topic) {
        this.kafka = kafka;
        this.json = json;
        this.topic = topic;
    }

    void publish(UUID userUuid, UUID requestEventId) {
        String body = json.writeValueAsString(new ProfileCreated("ProfileCreated", userUuid, requestEventId));
        try {
            kafka.send(topic, userUuid.toString(), body).get(6, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Profile completion publish interrupted", exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new IllegalStateException("Profile completion publish failed", exception);
        }
    }

    private record ProfileCreated(String eventType, UUID userUuid, UUID requestEventId) {
    }
}
