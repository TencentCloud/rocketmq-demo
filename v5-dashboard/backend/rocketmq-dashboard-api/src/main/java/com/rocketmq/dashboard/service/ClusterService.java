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
                .remark(instance.getRemark())
                .createTime(null)  // InstanceItem doesn't have CreateTime; use DescribeInstance for detail
                .updateTime(null)
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
     * Convert Unix timestamp (milliseconds) to LocalDateTime
     */
    private LocalDateTime convertTimestampMillis(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );
    }
    
    public ClusterInfo getCluster(String clusterId) throws TencentCloudSDKException {
        log.info("Getting cluster details for: {}", clusterId);
        
        try {
            DescribeInstanceRequest request = new DescribeInstanceRequest();
            request.setInstanceId(clusterId);
            
            DescribeInstanceResponse response = trocketClient.DescribeInstance(request);
            
            return mapDescribeInstanceToClusterInfo(response);
        } catch (TencentCloudSDKException e) {
            log.error("Failed to get cluster {} from Tencent Cloud API: {}", clusterId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Map DescribeInstanceResponse to ClusterInfo with full detail fields
     */
    private ClusterInfo mapDescribeInstanceToClusterInfo(DescribeInstanceResponse resp) {
        return ClusterInfo.builder()
                .clusterId(resp.getInstanceId())
                .clusterName(resp.getInstanceName())
                .description(resp.getRemark())
                .region(region)
                .clusterType(resp.getInstanceType() != null ? resp.getInstanceType() : "5.x")
                .status(mapInstanceStatus(resp.getInstanceStatus()))
                .maxTps(resp.getTpsLimit() != null ? resp.getTpsLimit().intValue() : null)
                .topicCount(resp.getTopicNum() != null ? resp.getTopicNum().intValue() : 0)
                .groupCount(resp.getGroupNum() != null ? resp.getGroupNum().intValue() : 0)
                .remark(resp.getRemark())
                .createTime(convertTimestampMillis(resp.getCreatedTime()))
                .updateTime(null)
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
        log.warn("Cluster creation is not allowed via dashboard");
        throw new UnsupportedOperationException("集群创建操作不被允许，请通过腾讯云控制台创建集群");
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
