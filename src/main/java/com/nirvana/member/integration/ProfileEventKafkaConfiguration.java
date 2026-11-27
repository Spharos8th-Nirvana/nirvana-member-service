package com.nirvana.member.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration(proxyBeanMethods = false)
class ProfileEventKafkaConfiguration {
    @Bean
    DefaultErrorHandler profileEventErrorHandler() {
        var handler = new DefaultErrorHandler(
                (record, exception) -> log.warn("Discarding invalid Kafka event. topic={} partition={} offset={}",
                        record.topic(), record.partition(), record.offset()),
                new FixedBackOff(5_000, FixedBackOff.UNLIMITED_ATTEMPTS));
        handler.addNotRetryableExceptions(ProfileCreationListener.InvalidProfileCreationEvent.class);
        return handler;
    }
}
