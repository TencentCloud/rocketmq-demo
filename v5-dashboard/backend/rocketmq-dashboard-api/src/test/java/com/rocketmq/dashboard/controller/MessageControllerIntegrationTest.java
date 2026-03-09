package com.rocketmq.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketmq.dashboard.dto.request.QueryMessagesRequest;
import com.rocketmq.dashboard.dto.request.SendMessageRequest;
import com.rocketmq.dashboard.dto.request.VerifyMessageRequest;
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
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void queryMessagesShouldReturnMessageList() throws Exception {
        QueryMessagesRequest request = new QueryMessagesRequest();
        request.setClusterId("rmq-cn-test12345");
        request.setTopicName("test-topic");

        mockMvc.perform(get("/api/v1/messages")
                        .param("clusterId", request.getClusterId())
                        .param("topicName", request.getTopicName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void queryMessagesWithKeyFilterShouldReturnFilteredList() throws Exception {
        mockMvc.perform(get("/api/v1/messages")
                        .param("clusterId", "rmq-cn-test12345")
                        .param("topicName", "test-topic")
                        .param("messageKey", "test-key-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void queryMessagesWithInvalidParametersShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/messages"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessageByIdShouldReturnMessageDetails() throws Exception {
        String messageId = "01000000000000000000000000000001";
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/messages/{id}", messageId)
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getMessageByIdWithoutClusterIdShouldReturnBadRequest() throws Exception {
        String messageId = "01000000000000000000000000000001";

        mockMvc.perform(get("/api/v1/messages/{id}", messageId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMessageTraceShouldReturnTraceInformation() throws Exception {
        String messageId = "01000000000000000000000000000001";
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/messages/{id}/trace", messageId)
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getMessageTraceWithoutClusterIdShouldReturnBadRequest() throws Exception {
        String messageId = "01000000000000000000000000000001";

        mockMvc.perform(get("/api/v1/messages/{id}/trace", messageId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendMessageShouldReturnSuccess() throws Exception {
        SendMessageRequest request = new SendMessageRequest();
        request.setClusterId("rmq-cn-test12345");
        request.setTopicName("test-topic");
        request.setBody("Test message body");
        request.setKeys("test-key-001");
        request.setTags("test-tag");

        mockMvc.perform(post("/api/v1/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Message sent successfully"));
    }

    @Test
    void sendMessageWithInvalidDataShouldReturnBadRequest() throws Exception {
        SendMessageRequest request = new SendMessageRequest();

        mockMvc.perform(post("/api/v1/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendDeadLetterMessageShouldReturnSuccess() throws Exception {
        String messageId = "01000000000000000000000000000001";
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(post("/api/v1/messages/{id}/resend", messageId)
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Dead letter message resent successfully"));
    }

    @Test
    void resendDeadLetterMessageWithoutClusterIdShouldReturnBadRequest() throws Exception {
        String messageId = "01000000000000000000000000000001";

        mockMvc.perform(post("/api/v1/messages/{id}/resend", messageId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyMessageConsumptionShouldReturnVerificationResult() throws Exception {
        VerifyMessageRequest request = new VerifyMessageRequest();
        request.setClusterId("rmq-cn-test12345");
        request.setMessageId("01000000000000000000000000000001");
        request.setConsumerGroup("test-consumer-group");

        mockMvc.perform(post("/api/v1/messages/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void verifyMessageConsumptionWithInvalidDataShouldReturnBadRequest() throws Exception {
        VerifyMessageRequest request = new VerifyMessageRequest();

        mockMvc.perform(post("/api/v1/messages/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
