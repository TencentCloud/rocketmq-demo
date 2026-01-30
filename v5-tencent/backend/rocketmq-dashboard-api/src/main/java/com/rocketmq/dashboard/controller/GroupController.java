package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.request.CreateGroupRequest;
import com.rocketmq.dashboard.dto.request.ResetOffsetRequest;
import com.rocketmq.dashboard.dto.request.UpdateGroupRequest;
import com.rocketmq.dashboard.dto.response.ConsumerClientInfo;
import com.rocketmq.dashboard.dto.response.ConsumerLagInfo;
import com.rocketmq.dashboard.dto.response.GroupInfo;
import com.rocketmq.dashboard.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/groups")
@Tag(name = "Consumer Group Management", description = "APIs for managing consumer groups")
public class GroupController {
    
    private final GroupService groupService;
    
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }
    
    @GetMapping
    @Operation(summary = "Get consumer group list", description = "Retrieve all consumer groups in a cluster")
    public Result<List<GroupInfo>> listGroups(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Listing consumer groups for cluster: {}", clusterId);
            List<GroupInfo> groups = groupService.listGroups(clusterId);
            return Result.success(groups);
        } catch (Exception e) {
            log.error("Failed to list consumer groups for cluster: {}", clusterId, e);
            throw new RuntimeException("Failed to list consumer groups: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{name}")
    @Operation(summary = "Get consumer group details", description = "Retrieve detailed information about a specific consumer group")
    public Result<GroupInfo> getGroup(
            @Parameter(description = "Group name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Getting consumer group: {} in cluster: {}", name, clusterId);
            GroupInfo group = groupService.getGroup(clusterId, name);
            return Result.success(group);
        } catch (Exception e) {
            log.error("Failed to get consumer group: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to get consumer group: " + e.getMessage(), e);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create consumer group", description = "Create a new consumer group in a cluster")
    public Result<GroupInfo> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        try {
            log.info("Creating consumer group: {} in cluster: {}", request.getGroupName(), request.getClusterId());
            GroupInfo group = groupService.createGroup(request);
            return Result.success("Consumer group created successfully", group);
        } catch (Exception e) {
            log.error("Failed to create consumer group: {}", request.getGroupName(), e);
            throw new RuntimeException("Failed to create consumer group: " + e.getMessage(), e);
        }
    }
    
    @PutMapping("/{name}")
    @Operation(summary = "Update consumer group", description = "Update consumer group configuration")
    public Result<GroupInfo> updateGroup(
            @Parameter(description = "Group name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Valid @RequestBody UpdateGroupRequest request) {
        try {
            log.info("Updating consumer group: {} in cluster: {}", name, clusterId);
            GroupInfo group = groupService.updateGroup(clusterId, name, request);
            return Result.success("Consumer group updated successfully", group);
        } catch (Exception e) {
            log.error("Failed to update consumer group: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to update consumer group: " + e.getMessage(), e);
        }
    }
    
    @DeleteMapping("/{name}")
    @Operation(summary = "Delete consumer group", description = "Delete a consumer group from a cluster")
    public Result<String> deleteGroup(
            @Parameter(description = "Group name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Deleting consumer group: {} from cluster: {}", name, clusterId);
            groupService.deleteGroup(clusterId, name);
            return Result.success("Consumer group deleted successfully", "OK");
        } catch (Exception e) {
            log.error("Failed to delete consumer group: {} from cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to delete consumer group: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{name}/clients")
    @Operation(summary = "Get consumer clients", description = "Retrieve list of consumer clients for a specific group")
    public Result<List<ConsumerClientInfo>> listClients(
            @Parameter(description = "Group name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Listing consumer clients for group: {} in cluster: {}", name, clusterId);
            List<ConsumerClientInfo> clients = groupService.getClients(clusterId, name);
            return Result.success(clients);
        } catch (Exception e) {
            log.error("Failed to list consumer clients for group: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to list consumer clients: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/{name}/reset")
    @Operation(summary = "Reset consumer offset", description = "Reset consumer offset for a specific group")
    public Result<String> resetOffset(
            @Parameter(description = "Group name", required = true)
            @PathVariable String name,
            @Valid @RequestBody ResetOffsetRequest request) {
        try {
            log.info("Resetting offset for group: {}", name);
            groupService.resetOffset(name, request);
            return Result.success("Offset reset successfully", "OK");
        } catch (Exception e) {
            log.error("Failed to reset offset for group: {}", name, e);
            throw new RuntimeException("Failed to reset offset: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{name}/lag")
    @Operation(summary = "Get consumer lag", description = "Retrieve consumer lag information for a specific group")
    public Result<List<ConsumerLagInfo>> getLag(
            @Parameter(description = "Group name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Getting consumer lag for group: {} in cluster: {}", name, clusterId);
            List<ConsumerLagInfo> lags = groupService.getLag(clusterId, name);
            return Result.success(lags);
        } catch (Exception e) {
            log.error("Failed to get consumer lag for group: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to get consumer lag: " + e.getMessage(), e);
        }
    }
}
