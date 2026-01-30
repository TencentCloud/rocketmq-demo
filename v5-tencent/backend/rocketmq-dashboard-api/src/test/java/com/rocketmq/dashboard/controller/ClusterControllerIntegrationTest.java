package com.rocketmq.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketmq.dashboard.dto.request.CreateClusterRequest;
import com.rocketmq.dashboard.dto.request.UpdateClusterRequest;
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
class ClusterControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listClustersShouldReturnSuccessWithEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/clusters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getClusterShouldReturnClusterDetails() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/clusters/{id}", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void createClusterShouldReturnSuccess() throws Exception {
        CreateClusterRequest request = new CreateClusterRequest();
        request.setClusterName("test-cluster");
        request.setRegion("ap-guangzhou");

        mockMvc.perform(post("/api/v1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cluster created successfully"));
    }

    @Test
    void createClusterWithInvalidDataShouldReturnBadRequest() throws Exception {
        CreateClusterRequest request = new CreateClusterRequest();
        // Missing required fields

        mockMvc.perform(post("/api/v1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateClusterShouldReturnBusinessError() throws Exception {
        String clusterId = "rmq-cn-test12345";
        UpdateClusterRequest request = new UpdateClusterRequest();
        request.setDescription("Updated description");

        mockMvc.perform(put("/api/v1/clusters/{id}", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1004))
                .andExpect(jsonPath("$.message").value("集群更新操作不被允许，请通过腾讯云控制台修改集群配置"));
    }

    @Test
    void updateClusterWithInvalidIdShouldReturnBusinessError() throws Exception {
        String clusterId = "non-existent-cluster";
        UpdateClusterRequest request = new UpdateClusterRequest();
        request.setDescription("test description");

        mockMvc.perform(put("/api/v1/clusters/{id}", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1004))
                .andExpect(jsonPath("$.message").value("集群更新操作不被允许，请通过腾讯云控制台修改集群配置"));
    }

    @Test
    void deleteClusterShouldReturnSuccess() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(delete("/api/v1/clusters/{id}", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cluster deleted successfully"));
    }

    @Test
    void deleteClusterWithInvalidIdShouldHandleGracefully() throws Exception {
        String clusterId = "non-existent-cluster";

        mockMvc.perform(delete("/api/v1/clusters/{id}", clusterId))
                .andExpect(status().isOk());
    }
}
