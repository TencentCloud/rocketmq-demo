package com.rocketmq.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketmq.dashboard.dto.request.CreateRoleRequest;
import com.rocketmq.dashboard.dto.request.UpdateRoleRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listRolesShouldReturnRoleList() throws Exception {
        String clusterId = "rmq-cn-test12345";

        mockMvc.perform(get("/api/v1/roles")
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listRolesWithoutClusterIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRoleShouldReturnRoleDetails() throws Exception {
        String clusterId = "rmq-cn-test12345";
        String roleName = "test-role";

        mockMvc.perform(get("/api/v1/roles/{name}", roleName)
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void getRoleWithoutClusterIdShouldReturnBadRequest() throws Exception {
        String roleName = "test-role";

        mockMvc.perform(get("/api/v1/roles/{name}", roleName))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoleShouldReturnSuccess() throws Exception {
        String clusterId = "rmq-cn-test12345";
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName("new-test-role");
        request.setPermissions(Arrays.asList("PUB", "SUB"));

        mockMvc.perform(post("/api/v1/roles")
                        .param("clusterId", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Role created successfully"));
    }

    @Test
    void createRoleWithInvalidDataShouldReturnBadRequest() throws Exception {
        String clusterId = "rmq-cn-test12345";
        CreateRoleRequest request = new CreateRoleRequest();

        mockMvc.perform(post("/api/v1/roles")
                        .param("clusterId", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoleWithoutClusterIdShouldReturnBadRequest() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setRoleName("test-role");
        request.setPermissions(Arrays.asList("PUB"));

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRoleShouldReturnSuccess() throws Exception {
        String clusterId = "rmq-cn-test12345";
        String roleName = "test-role";
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setPermissions(Arrays.asList("PUB", "SUB", "DENY"));
        request.setDescription("Updated role description");

        mockMvc.perform(put("/api/v1/roles/{name}", roleName)
                        .param("clusterId", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Role updated successfully"));
    }

    @Test
    void updateRoleWithInvalidDataShouldReturnBadRequest() throws Exception {
        String clusterId = "rmq-cn-test12345";
        String roleName = "test-role";
        UpdateRoleRequest request = new UpdateRoleRequest();

        mockMvc.perform(put("/api/v1/roles/{name}", roleName)
                        .param("clusterId", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRoleWithoutClusterIdShouldReturnBadRequest() throws Exception {
        String roleName = "test-role";
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setPermissions(Arrays.asList("SUB"));

        mockMvc.perform(put("/api/v1/roles/{name}", roleName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRoleShouldReturnSuccess() throws Exception {
        String clusterId = "rmq-cn-test12345";
        String roleName = "test-role-to-delete";

        mockMvc.perform(delete("/api/v1/roles/{name}", roleName)
                        .param("clusterId", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));
    }

    @Test
    void deleteRoleWithoutClusterIdShouldReturnBadRequest() throws Exception {
        String roleName = "test-role";

        mockMvc.perform(delete("/api/v1/roles/{name}", roleName))
                .andExpect(status().isBadRequest());
    }
}
