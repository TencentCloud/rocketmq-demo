package com.rocketmq.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketmq.dashboard.dto.request.CredentialsRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void setCredentialsShouldReturnSuccess() throws Exception {
        CredentialsRequest request = new CredentialsRequest();
        request.setSecretId("test-secret-id");
        request.setSecretKey("test-secret-key");
        request.setRegion("ap-guangzhou");

        mockMvc.perform(post("/api/v1/config/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Credentials configured successfully"));
    }

    @Test
    void getCredentialsShouldReturnMaskedSecretId() throws Exception {
        CredentialsRequest request = new CredentialsRequest();
        request.setSecretId("AKID1234567890ABCDEF");
        request.setSecretKey("test-secret-key");
        request.setRegion("ap-shanghai");

        mockMvc.perform(post("/api/v1/config/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/v1/config/credentials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.configured").value(true))
                .andExpect(jsonPath("$.data.secretId").value("AKID****CDEF"))
                .andExpect(jsonPath("$.data.region").value("ap-shanghai"));
    }

    @Test
    void clearCredentialsShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/config/credentials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Credentials cleared successfully"));
    }

    @Test
    void getRegionsShouldReturnAvailableRegions() throws Exception {
        mockMvc.perform(get("/api/v1/config/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].regionId").value("ap-guangzhou"))
                .andExpect(jsonPath("$.data[0].regionName").value("Guangzhou"))
                .andExpect(jsonPath("$.data[0].available").value(true));
    }

    @Test
    void setCredentialsWithInvalidDataShouldReturnBadRequest() throws Exception {
        CredentialsRequest request = new CredentialsRequest();

        mockMvc.perform(post("/api/v1/config/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
