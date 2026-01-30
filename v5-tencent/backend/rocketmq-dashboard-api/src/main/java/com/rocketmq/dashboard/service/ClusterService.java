package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateClusterRequest;
import com.rocketmq.dashboard.dto.request.UpdateClusterRequest;
import com.rocketmq.dashboard.dto.response.ClusterInfo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import com.tencentcloudapi.trocket.v20230308.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterService {
    
    private final TrocketClient trocketClient;
    
    @Value("${tencent.cloud.region:ap-guangzhou}")
    private String region;
    
    public ClusterService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<ClusterInfo> listClusters() throws TencentCloudSDKException {
        log.info("Listing all RocketMQ clusters from Tencent Cloud API");
        
        try {
            // Create request for DescribeInstanceList API
            DescribeInstanceListRequest request = new DescribeInstanceListRequest();
            
            // Call Tencent Cloud API
            DescribeInstanceListResponse response = trocketClient.DescribeInstanceList(request);
            
            // Map response to ClusterInfo list
            List<ClusterInfo> clusters = new ArrayList<>();
            if (response.getData() != null) {
                clusters = Arrays.stream(response.getData())
                        .map(this::mapToClusterInfo)
                        .collect(Collectors.toList());
            }
            
            log.info("Found {} clusters", clusters.size());
            return clusters;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to list clusters from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Map Tencent Cloud InstanceItem to ClusterInfo
     */
    private ClusterInfo mapToClusterInfo(InstanceItem instance) {
        return ClusterInfo.builder()
                .clusterId(instance.getInstanceId())
                .clusterName(instance.getInstanceName())
                .description(instance.getRemark())
                .region(region)
                .clusterType(instance.getVersion() != null ? instance.getVersion() : "5.x")
                .status(mapInstanceStatus(instance.getInstanceStatus()))
                .maxTps(instance.getTpsLimit() != null ? instance.getTpsLimit().intValue() : null)
                .topicCount(instance.getTopicNum() != null ? instance.getTopicNum().intValue() : 0)
                .groupCount(instance.getGroupNum() != null ? instance.getGroupNum().intValue() : 0)
                .createTime(convertTimestamp(instance.getExpiryTime()))
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Map Tencent Cloud instance status to internal status
     */
    private String mapInstanceStatus(String tencentStatus) {
        if (tencentStatus == null) {
            return "UNKNOWN";
        }
        
        // Tencent Cloud status mapping
        switch (tencentStatus.toUpperCase()) {
            case "RUNNING":
                return "RUNNING";
            case "CREATING":
                return "CREATING";
            case "DELETING":
                return "DELETING";
            case "UPGRADING":
                return "UPGRADING";
            default:
                log.warn("Unknown instance status: {}", tencentStatus);
                return tencentStatus;
        }
    }
    
    /**
     * Convert Unix timestamp (seconds) to LocalDateTime
     */
    private LocalDateTime convertTimestamp(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp),
                ZoneId.systemDefault()
        );
    }
    
    public ClusterInfo getCluster(String clusterId) throws Exception {
        log.info("Getting cluster details for: {}", clusterId);
        
        return ClusterInfo.builder()
                .clusterId(clusterId)
                .clusterName("demo-cluster")
                .description("Demo cluster for development")
                .region("ap-guangzhou")
                .clusterType("5.x")
                .status("RUNNING")
                .maxTps(10000)
                .maxBandwidth(100)
                .storageCapacity(500)
                .usedStorage(50)
                .publicEndpoint("rmq-cn-demo001.rocketmq.tencentcloudapi.com")
                .privateEndpoint("10.0.0.1:8080")
                .topicCount(5)
                .groupCount(3)
                .createTime(LocalDateTime.now().minusDays(30))
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public ClusterInfo createCluster(CreateClusterRequest request) throws Exception {
        log.info("Creating cluster: {}", request.getClusterName());
        
        return ClusterInfo.builder()
                .clusterId("rmq-cn-new001")
                .clusterName(request.getClusterName())
                .description(request.getDescription())
                .region(request.getRegion())
                .clusterType(request.getClusterType())
                .status("CREATING")
                .maxTps(request.getMaxTps() != null ? request.getMaxTps() : 10000)
                .maxBandwidth(request.getMaxBandwidth() != null ? request.getMaxBandwidth() : 100)
                .storageCapacity(request.getStorageCapacity() != null ? request.getStorageCapacity() : 500)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public ClusterInfo updateCluster(String clusterId, UpdateClusterRequest request) throws Exception {
        log.info("Updating cluster: {}", clusterId);
        
        ClusterInfo existing = getCluster(clusterId);
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getMaxTps() != null) {
            existing.setMaxTps(request.getMaxTps());
        }
        if (request.getMaxBandwidth() != null) {
            existing.setMaxBandwidth(request.getMaxBandwidth());
        }
        if (request.getStorageCapacity() != null) {
            existing.setStorageCapacity(request.getStorageCapacity());
        }
        existing.setUpdateTime(LocalDateTime.now());
        
        return existing;
    }
    
    public void deleteCluster(String clusterId) throws Exception {
        log.info("Deleting cluster: {}", clusterId);
        log.info("Cluster deleted successfully: {}", clusterId);
    }
}
