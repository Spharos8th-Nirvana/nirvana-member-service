package com.nirvana.member.integration;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nirvana.member.repository.UserProfileRepository;
import com.nirvana.member.service.UserProfileService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import tools.jackson.databind.json.JsonMapper;

class ProfileCreationListenerTest {
    private static final ValidatorFactory VALIDATORS = Validation.buildDefaultValidatorFactory();
    private static final String USER_UUID = "74497355-d7cb-494f-8220-e9eb186c1e8c";
    private static final String EVENT = """
            {"eventId":"bc3988dd-fd56-4eed-b359-ee6d63b7076a","eventType":"ProfileCreationRequested",
             "userUuid":"74497355-d7cb-494f-8220-e9eb186c1e8c","name":"테스트 회원","countryCode":"kr"}
            """;

    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final ProfileCreatedPublisher completed = mock(ProfileCreatedPublisher.class);
    private final ProfileCreationListener listener = new ProfileCreationListener(
            JsonMapper.builder().build(), VALIDATORS.getValidator(), new UserProfileService(profiles, transactions),
            completed);

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATORS.close();
    }

    @BeforeEach
    void openTransaction() {
        when(transactions.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
    }

    @Test
    void createsProfileAndPreservesItOnRedelivery() {
        when(profiles.existsByUserUuid(USER_UUID)).thenReturn(false, true, true);
        listener.receive(record(EVENT));
        listener.receive(record(EVENT));
        listener.receive(record(EVENT.replace("bc3988dd-fd56-4eed-b359-ee6d63b7076a",
                "b6a975dd-201f-41b1-abab-25a3c5c8be97")));

        verify(profiles).saveAndFlush(argThat(profile -> profile.getUserUuid().toString().equals(USER_UUID)
                && profile.getName().equals("테스트 회원") && profile.getCountryCode().equals("KR")
                && profile.getNickname().matches("회원-[0-9a-f]{16}")));
        verify(profiles, times(3)).existsByUserUuid(USER_UUID);
        verify(profiles).countActiveNickname(anyString());
    }

    @Test
    void retriesWithAnotherNicknameWhenInsertCollides() {
        when(profiles.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate nickname",
                        new SQLException("Duplicate entry for key 'userProfiles.UK_userProfiles_nicknameActive'",
                                "23000", 1062)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        listener.receive(record(EVENT));

        var nicknames = ArgumentCaptor.forClass(String.class);
        verify(profiles, times(2)).countActiveNickname(nicknames.capture());
        assertNotEquals(nicknames.getAllValues().get(0), nicknames.getAllValues().get(1));
        verify(profiles, times(2)).saveAndFlush(any());
    }

    @Test
    void triesCompletionAgainWhenPublishingFailsAfterProfileCommit() {
        var userUuid = UUID.fromString(USER_UUID);
        var requestId = UUID.fromString("bc3988dd-fd56-4eed-b359-ee6d63b7076a");
        when(profiles.existsByUserUuid(USER_UUID)).thenReturn(false, true);
        doThrow(new IllegalStateException("Kafka unavailable")).doNothing()
                .when(completed).publish(userUuid, requestId);

        assertThrows(IllegalStateException.class, () -> listener.receive(record(EVENT)));
        listener.receive(record(EVENT));

        var order = org.mockito.Mockito.inOrder(profiles, transactions, completed);
        order.verify(profiles).saveAndFlush(any());
        order.verify(transactions).commit(any());
        order.verify(completed).publish(userUuid, requestId);
        verify(profiles).saveAndFlush(any());
        verify(completed, times(2)).publish(userUuid, requestId);
    }

    @Test
    void skipsOtherEventTypesAndRejectsInvalidProfileData() {
        listener.receive(record(EVENT.replace("ProfileCreationRequested", "UserWithdrawn")));
        assertThrows(ProfileCreationListener.InvalidProfileCreationEvent.class,
                () -> listener.receive(record(EVENT.replace("\"kr\"", "\"zz\""))));
        verifyNoInteractions(profiles, completed);
    }

    private static ConsumerRecord<String, String> record(String body) {
        return new ConsumerRecord<>("nirvana.auth.user", 0, 0L, USER_UUID, body);
    }
}
