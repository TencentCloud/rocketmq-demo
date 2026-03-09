package com.rocketmq.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getOverviewShouldReturnDashboardStatistics() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/dashboard/overview")
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getOverviewWithoutClusterIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrendsShouldReturnTimeSeriesData() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/dashboard/trends")
                        .param("clusterId", clusterId)
                        .param("timeRange", "24h"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getTrendsWithDefaultTimeRangeShouldSucceed() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/dashboard/trends")
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTrendsWithDifferentTimeRangesShouldSucceed() throws Exception {
        String clusterId = "rmq-cn-test12345";
        String[] timeRanges = {"24h", "7d", "30d"};

        for (String timeRange : timeRanges) {
            mockMvc.perform(get("/api/v1/dashboard/trends")
                            .param("clusterId", clusterId)
                            .param("timeRange", timeRange))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Test
    void getTrendsWithoutClusterIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/trends"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopLagGroupsShouldReturnConsumerGroups() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/dashboard/top-lag")
                        .param("clusterId", clusterId)
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTopLagGroupsWithDefaultLimitShouldSucceed() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/dashboard/top-lag")
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTopLagGroupsWithCustomLimitShouldSucceed() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/dashboard/top-lag")
                        .param("clusterId", clusterId)
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTopLagGroupsWithoutClusterIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/top-lag"))
                .andExpect(status().isBadRequest());
    }
}
