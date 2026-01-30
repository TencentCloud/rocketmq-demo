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
    
    public ClusterInfo getCluster(String clusterId) throws TencentCloudSDKException {
        log.info("Getting cluster details for: {}", clusterId);
        
        try {
            // Create request for DescribeInstanceList API with filter
            DescribeInstanceListRequest request = new DescribeInstanceListRequest();
            
            // Filter by specific instance ID
            Filter[] filters = new Filter[1];
            filters[0] = new Filter();
            filters[0].setName("InstanceId");
            filters[0].setValues(new String[]{clusterId});
            request.setFilters(filters);
            
            // Call Tencent Cloud API
            DescribeInstanceListResponse response = trocketClient.DescribeInstanceList(request);
            
            // Check if instance exists
            if (response.getData() == null || response.getData().length == 0) {
                log.error("Cluster not found: {}", clusterId);
                throw new TencentCloudSDKException("Cluster not found: " + clusterId);
            }
            
            // Get the first (and should be only) instance
            InstanceItem instance = response.getData()[0];
            
            // Map to ClusterInfo with detailed information
            ClusterInfo clusterInfo = mapToClusterInfoDetailed(instance);
            
            log.info("Successfully retrieved cluster details: {}", clusterId);
            return clusterInfo;
            
        } catch (TencentCloudSDKException e) {
            log.error("Failed to get cluster {} from Tencent Cloud API: {}", clusterId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Map Tencent Cloud InstanceItem to ClusterInfo with detailed fields
     */
    private ClusterInfo mapToClusterInfoDetailed(InstanceItem instance) {
        return ClusterInfo.builder()
                .clusterId(instance.getInstanceId())
                .clusterName(instance.getInstanceName())
                .description(instance.getRemark())
                .region(region)
                .clusterType(instance.getVersion() != null ? instance.getVersion() : "5.x")
                .status(mapInstanceStatus(instance.getInstanceStatus()))
                .vpcId(null)  // Not available in InstanceItem
                .subnetId(null)  // Not available in InstanceItem
                .maxTps(instance.getTpsLimit() != null ? instance.getTpsLimit().intValue() : null)
                .maxBandwidth(null)  // Not available in InstanceItem
                .storageCapacity(null)  // Not available in InstanceItem
                .usedStorage(null)  // Not available in InstanceItem
                .publicEndpoint(buildEndpoint(instance.getInstanceId(), "public"))
                .privateEndpoint(buildEndpoint(instance.getInstanceId(), "private"))
                .topicCount(instance.getTopicNum() != null ? instance.getTopicNum().intValue() : 0)
                .groupCount(instance.getGroupNum() != null ? instance.getGroupNum().intValue() : 0)
                .createTime(convertTimestamp(instance.getExpiryTime()))
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Build endpoint URL for instance
     */
    private String buildEndpoint(String instanceId, String type) {
        if (instanceId == null) {
            return null;
        }
        // Construct endpoint based on Tencent Cloud format
        // Format: {instanceId}.{type}.rocketmq.{region}.tencentcloudapi.com
        if ("public".equals(type)) {
            return String.format("%s.rocketmq.%s.tencentcloudapi.com", instanceId, region);
        } else {
            // Private endpoint format (VPC internal)
            return String.format("%s.internal.rocketmq.%s.tencentcloudapi.com", instanceId, region);
        }
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
        log.warn("Cluster update is not allowed via dashboard: {}", clusterId);
        throw new com.rocketmq.dashboard.exception.BusinessException(
            com.rocketmq.dashboard.common.ResponseCode.BUSINESS_ERROR,
            "集群更新操作不被允许，请通过腾讯云控制台修改集群配置"
        );
    }
    
    public void deleteCluster(String clusterId) throws Exception {
        log.warn("Cluster deletion is not allowed via dashboard: {}", clusterId);
        throw new UnsupportedOperationException("集群删除操作不被允许,请通过腾讯云控制台删除集群");
    }
}
