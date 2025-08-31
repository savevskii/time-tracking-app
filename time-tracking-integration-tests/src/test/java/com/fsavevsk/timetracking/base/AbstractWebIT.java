package com.fsavevsk.timetracking.base;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsavevsk.timetracking.config.PostgresTestConfig;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({ PostgresTestConfig.class, TestSecurityConfig.class })
@ActiveProfiles("test")
public abstract class AbstractWebIT {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected ProjectRepository projectRepository;
    @Autowired protected TimeEntryRepository timeEntryRepository;

    protected static final String BEARER_TOKEN = "Bearer it-token";


    protected <T> T performGetRequest(String url, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        var res = mockMvc.perform(
                        MockMvcRequestBuilders.get(url)
                                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(expectedStatus)
                .andReturn();
        String json = res.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(json, responseType);
    }

    protected <T> T performGetRequest(String url,
                                      TypeReference<T> typeRef,
                                      ResultMatcher expectedStatus) throws Exception {
        var res = mockMvc.perform(
                        MockMvcRequestBuilders.get(url)
                                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(expectedStatus)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        String json = res.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(json, typeRef);
    }

    protected <T> T performPostRequest(String url, Object body, Class<T> responseType, ResultMatcher expectedStatus) throws Exception {
        var res = mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(expectedStatus)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        String json = res.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(json, responseType);
    }

    protected void performDeleteRequestNoContent(String url, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(expectedStatus)
                .andReturn();
    }

    @BeforeEach
    void resetDb() {
        timeEntryRepository.deleteAllInBatch();
        projectRepository.deleteAllInBatch();
    }

}
