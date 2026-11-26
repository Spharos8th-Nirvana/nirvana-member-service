package com.nirvana.member.profile.controller;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nirvana.member.profile.domain.UserProfile;
import com.nirvana.member.profile.repository.UserProfileRepository;
import java.sql.SQLException;
import java.util.UUID;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.templates.TemplateFormats;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = {
    "member.access.auth-secret=example-service-token",
    "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
            + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
            + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"
})
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class UserProfileApiTest {
    private static final UUID USER_UUID = UUID.fromString("74497355-d7cb-494f-8220-e9eb186c1e8c");
    private static final String PATH = "/internal/profiles/{userUuid}";
    private static final String BODY = """
            {"name":"테스트 회원","nickname":"산책","countryCode":"kr"}
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc applicationMvc;

    @MockitoBean
    private UserProfileRepository profiles;

    @MockitoBean
    private PlatformTransactionManager transactionManager;

    private MockMvc mvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation).snippets()
                        .withTemplateFormat(TemplateFormats.markdown()))
                .build();
        when(transactionManager.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
    }

    @Test
    void createsProfileAndNormalizesCountryCode() throws Exception {
        when(profiles.saveAndFlush(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        submit(BODY)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userUuid").value(USER_UUID.toString()))
                .andExpect(jsonPath("$.data.name").value("테스트 회원"))
                .andExpect(jsonPath("$.data.nickname").value("산책"))
                .andExpect(jsonPath("$.data.countryCode").value("KR"))
                .andDo(document("profiles/created"));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        expectError(submit("{}"), 400, "VALIDATION_FAILED", "validation-failed");
    }

    @Test
    void rejectsMalformedCountryCode() throws Exception {
        submit(BODY.replace("kr", "KOR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
    }

    @Test
    void rejectsUnknownIsoCountryCodeBeforeCheckingConflicts() throws Exception {
        expectError(submit(BODY.replace("kr", "ZZ")), 400, "INVALID_COUNTRY_CODE", "invalid-country-code");
        verifyNoInteractions(profiles);
    }

    @Test
    void rejectsMissingServiceToken() throws Exception {
        expectError(mvc.perform(post(PATH, USER_UUID).contentType(MediaType.APPLICATION_JSON).content(BODY)),
                401, "AUTHENTICATION_REQUIRED", "authentication-required");
    }

    @Test
    void returnsExistingProfileWithoutAttemptingInsert() throws Exception {
        when(profiles.existsByUserUuid(USER_UUID.toString())).thenReturn(true);

        submit(BODY).andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PROFILE_ALREADY_EXISTS"));

        verify(profiles, never()).saveAndFlush(any(UserProfile.class));
    }

    @Test
    void reportsExistingProfileWhenConcurrentInsertViolatesNicknameKey() throws Exception {
        var failedWrite = new SimpleTransactionStatus();
        var freshRead = new SimpleTransactionStatus();
        when(transactionManager.getTransaction(any())).thenReturn(failedWrite, freshRead);
        when(profiles.existsByUserUuid(USER_UUID.toString())).thenReturn(false, true);
        rejectInsertWithDuplicateNickname();

        expectError(submit(BODY), 409, "PROFILE_ALREADY_EXISTS", "profile-already-exists");

        var ordered = inOrder(transactionManager, profiles);
        ordered.verify(transactionManager).rollback(failedWrite);
        ordered.verify(transactionManager).getTransaction(
                argThat(definition ->
                        definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        ordered.verify(profiles).existsByUserUuid(USER_UUID.toString());
        ordered.verify(transactionManager).commit(freshRead);
    }

    @Test
    void reportsNicknameTakenWhenConcurrentInsertBelongsToAnotherProfile() throws Exception {
        rejectInsertWithDuplicateNickname();

        expectError(submit(BODY), 409, "NICKNAME_TAKEN", "nickname-taken");
    }

    @Test
    void reportsExistingProfileWhenInsertViolatesUuidKey() throws Exception {
        rejectInsertWithDuplicateKey("UK_userProfiles_userUuid");

        submit(BODY)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("PROFILE_ALREADY_EXISTS"));
    }

    @Test
    void doesNotClassifyAnUnknownUniqueConstraintAsProfileConflict() throws Exception {
        rejectInsertWithDuplicateKey("userProfiles.UK_userProfiles_other");

        submit(BODY)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }

    @Test
    void doesNotInferAConstraintFromExceptionTextWhenHibernateHasNoName() throws Exception {
        var sql = new SQLException(
                "Duplicate entry '산책' for key 'userProfiles.UK_userProfiles_nicknameActive'", "23000", 1062);
        var violation = new ConstraintViolationException("duplicate key", sql, ConstraintKind.UNIQUE, null);
        when(profiles.saveAndFlush(any(UserProfile.class)))
                .thenThrow(new DataIntegrityViolationException("test unnamed constraint", violation));

        submit(BODY)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"));
    }

    @Test
    void reportsUnexpectedStorageFailure() throws Exception {
        when(profiles.saveAndFlush(any(UserProfile.class)))
                .thenThrow(new DataIntegrityViolationException("test storage error"));

        expectError(submit(BODY), 500, "INTERNAL_ERROR", "internal-error");
    }

    @Test
    void exposesInterfaceDocumentationInOpenApi() throws Exception {
        String operation = "$.paths['/internal/profiles/{userUuid}'].post";

        applicationMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(operation + ".summary").value("일반 회원 프로필 생성"))
                .andExpect(jsonPath(operation + ".parameters[?(@.name == 'X-Member-Service-Token')].required")
                        .value(contains(true)))
                .andExpect(jsonPath(operation + ".requestBody.required").value(true))
                .andExpect(jsonPath(operation + ".responses['201'].content['application/json']").exists())
                .andExpect(jsonPath(operation + ".responses['400']").exists())
                .andExpect(jsonPath(operation + ".responses['401']").exists())
                .andExpect(jsonPath(operation + ".responses['409'].description")
                        .value("PROFILE_ALREADY_EXISTS 또는 NICKNAME_TAKEN"))
                .andExpect(jsonPath(operation + ".responses['500']").exists());
    }

    private ResultActions submit(String body) throws Exception {
        return mvc.perform(post(PATH, USER_UUID)
                .header(UserProfileApi.SERVICE_CREDENTIAL, "example-service-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void rejectInsertWithDuplicateNickname() {
        rejectInsertWithDuplicateKey("userProfiles.UK_userProfiles_nicknameActive");
    }

    private void rejectInsertWithDuplicateKey(String key) {
        var sql = new SQLException("Duplicate entry 'value' for key '" + key + "'", "23000", 1062);
        var violation = new MySQLDialect().buildSQLExceptionConversionDelegate()
                .convert(sql, "test duplicate key", "insert into userProfiles");
        when(profiles.saveAndFlush(any(UserProfile.class)))
                .thenThrow(new DataIntegrityViolationException("test duplicate key", violation));
    }

    private void expectError(ResultActions result, int statusCode, String errorCode, String snippet) throws Exception {
        result.andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(errorCode))
                .andDo(document("profiles/" + snippet));
    }
}
