package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.request.CreateRoleRequest;
import com.rocketmq.dashboard.dto.request.UpdateRoleRequest;
import com.rocketmq.dashboard.dto.response.RoleInfo;
import com.rocketmq.dashboard.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Role Management", description = "APIs for managing RocketMQ access roles")
public class RoleController {
    
    private final RoleService roleService;
    
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    @GetMapping
    @Operation(summary = "Get role list", description = "Retrieve all access roles for a cluster")
    public Result<List<RoleInfo>> listRoles(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Listing all roles for cluster: {}", clusterId);
            List<RoleInfo> roles = roleService.listRoles(clusterId);
            return Result.success(roles);
        } catch (Exception e) {
            log.error("Failed to list roles", e);
            throw new RuntimeException("Failed to list roles: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{name}")
    @Operation(summary = "Get role details", description = "Retrieve detailed information about a specific role")
    public Result<RoleInfo> getRole(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Role name", required = true, example = "my-role")
            @PathVariable String name) {
        try {
            log.info("Getting role details for: {}", name);
            RoleInfo role = roleService.getRole(clusterId, name);
            return Result.success(role);
        } catch (Exception e) {
            log.error("Failed to get role: {}", name, e);
            throw new RuntimeException("Failed to get role: " + e.getMessage(), e);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create role", description = "Create a new access role")
    public Result<RoleInfo> createRole(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Valid @RequestBody CreateRoleRequest request) {
        try {
            log.info("Creating role: {}", request.getRoleName());
            RoleInfo role = roleService.createRole(clusterId, request);
            return Result.success("Role created successfully", role);
        } catch (Exception e) {
            log.error("Failed to create role", e);
            throw new RuntimeException("Failed to create role: " + e.getMessage(), e);
        }
    }
    
    @PutMapping("/{name}")
    @Operation(summary = "Update role", description = "Update role configuration")
    public Result<RoleInfo> updateRole(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Role name", required = true, example = "my-role")
            @PathVariable String name,
            @Valid @RequestBody UpdateRoleRequest request) {
        try {
            log.info("Updating role: {}", name);
            RoleInfo role = roleService.updateRole(clusterId, name, request);
            return Result.success("Role updated successfully", role);
        } catch (Exception e) {
            log.error("Failed to update role: {}", name, e);
            throw new RuntimeException("Failed to update role: " + e.getMessage(), e);
        }
    }
    
    @DeleteMapping("/{name}")
    @Operation(summary = "Delete role", description = "Delete an access role")
    public Result<String> deleteRole(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Role name", required = true, example = "my-role")
            @PathVariable String name) {
        try {
            log.info("Deleting role: {}", name);
            roleService.deleteRole(clusterId, name);
            return Result.success("Role deleted successfully", "OK");
        } catch (Exception e) {
            log.error("Failed to delete role: {}", name, e);
            throw new RuntimeException("Failed to delete role: " + e.getMessage(), e);
        }
    }
}
