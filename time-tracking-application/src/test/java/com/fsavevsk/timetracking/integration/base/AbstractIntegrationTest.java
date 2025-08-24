package com.fsavevsk.timetracking.integration.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fsavevsk.timetracking.security.TestJwtConfig;
import com.fsavevsk.timetracking.security.TestJwtFactory;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestJwtConfig.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest extends TestFactory {

    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired protected MockMvc mockMvc;
    @Autowired JwtEncoder jwtEncoder;

    private volatile String cachedToken;

    protected final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** Create once, reuse for all requests in this test class. Thread-safe. */
    protected String token() {
        String t = cachedToken;
        if (t != null) return t;
        synchronized (this) {
            if (cachedToken == null) {
                cachedToken = TestJwtFactory.token(
                        jwtEncoder,
                        "it-user",
                        Map.of("realm_access", Map.of("roles", List.of("admin", "user")))
                );
            }
            return cachedToken;
        }
    }

    protected RequestPostProcessor auth() {
        return request -> { request.addHeader("Authorization", "Bearer " + token()); return request; };
    }

    protected <T> T performGetRequest(String path, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.get(path)
                        .with(auth()))
                .andExpect(expectedStatus)
                .andReturn();
        return convertStringToClass(res.getResponse().getContentAsString(), responseType);
    }

    protected <T> T performGetRequest(String path, TypeReference<T> typeRef, ResultMatcher expectedStatus) throws Exception {
        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.get(path)
                        .with(auth()))
                .andExpect(expectedStatus)
                .andReturn();
        return mapper.readValue(res.getResponse().getContentAsString(), typeRef);
    }

    protected <T> T performPostRequest(String path, Object body, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        MvcResult mvcResult = getResultActions(path, body)
                .andExpect(expectedStatus)
                .andReturn();
        return convertStringToClass(mvcResult.getResponse().getContentAsString(), responseType);
    }

    protected void performDeleteRequestNoContent(String path, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(path)
                        .with(auth()))
                .andExpect(expectedStatus);
    }

    private ResultActions getResultActions(String path, Object object) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(path)
                        .with(auth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(object)));
    }

    private <T> T convertStringToClass(String jsonString, Class<T> responseType) throws JsonProcessingException {
        return mapper.readValue(jsonString, responseType);
    }
}
