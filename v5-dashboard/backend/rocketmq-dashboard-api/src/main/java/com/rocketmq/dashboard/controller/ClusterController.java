package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.request.CreateClusterRequest;
import com.rocketmq.dashboard.dto.request.UpdateClusterRequest;
import com.rocketmq.dashboard.dto.response.ClusterInfo;
import com.rocketmq.dashboard.service.ClusterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/clusters")
@Tag(name = "Cluster Management", description = "APIs for managing RocketMQ clusters")
public class ClusterController {
    
    private final ClusterService clusterService;
    
    public ClusterController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }
    
    @GetMapping
    @Operation(summary = "Get cluster list", description = "Retrieve all RocketMQ clusters")
    public Result<List<ClusterInfo>> listClusters() {
        try {
            log.info("Listing all clusters");
            List<ClusterInfo> clusters = clusterService.listClusters();
            return Result.success(clusters);
        } catch (Exception e) {
            log.error("Failed to list clusters", e);
            throw new RuntimeException("Failed to list clusters: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get cluster details", description = "Retrieve detailed information about a specific cluster")
    public Result<ClusterInfo> getCluster(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @PathVariable String id) {
        try {
            log.info("Getting cluster: {}", id);
            ClusterInfo cluster = clusterService.getCluster(id);
            return Result.success(cluster);
        } catch (Exception e) {
            log.error("Failed to get cluster: {}", id, e);
            throw new RuntimeException("Failed to get cluster: " + e.getMessage(), e);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create cluster", description = "Create a new RocketMQ cluster")
    public Result<ClusterInfo> createCluster(@Valid @RequestBody CreateClusterRequest request) {
        try {
            log.info("Creating cluster: {}", request.getClusterName());
            ClusterInfo cluster = clusterService.createCluster(request);
            return Result.success("Cluster created successfully", cluster);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create cluster", e);
            throw new RuntimeException("Failed to create cluster: " + e.getMessage(), e);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update cluster", description = "Update cluster configuration")
    public Result<ClusterInfo> updateCluster(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @PathVariable String id,
            @Valid @RequestBody UpdateClusterRequest request) throws Exception {
        log.info("Updating cluster: {}", id);
        ClusterInfo cluster = clusterService.updateCluster(id, request);
        return Result.success("Cluster updated successfully", cluster);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete cluster", description = "Delete a RocketMQ cluster")
    public Result<String> deleteCluster(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @PathVariable String id) {
        try {
            log.info("Deleting cluster: {}", id);
            clusterService.deleteCluster(id);
            return Result.success("Cluster deleted successfully", "OK");
        } catch (Exception e) {
            log.error("Failed to delete cluster: {}", id, e);
            throw new RuntimeException("Failed to delete cluster: " + e.getMessage(), e);
        }
    }
}
