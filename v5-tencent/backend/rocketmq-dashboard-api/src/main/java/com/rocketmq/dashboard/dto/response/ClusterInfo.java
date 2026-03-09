package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cluster information")
public class ClusterInfo {
    
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx")
    private String clusterId;
    
    @Schema(description = "Cluster name", example = "my-rocketmq-cluster")
    private String clusterName;
    
    @Schema(description = "Cluster description")
    private String description;
    
    @Schema(description = "Region", example = "ap-guangzhou")
    private String region;
    
    @Schema(description = "Cluster type", example = "5.x")
    private String clusterType;
    
    @Schema(description = "Cluster status: CREATING, RUNNING, UPGRADING, DELETING", example = "RUNNING")
    private String status;
    
    @Schema(description = "VPC ID")
    private String vpcId;
    
    @Schema(description = "Subnet ID")
    private String subnetId;
    
    @Schema(description = "Maximum TPS")
    private Integer maxTps;
    
    @Schema(description = "Maximum bandwidth in MB/s")
    private Integer maxBandwidth;
    
    @Schema(description = "Storage capacity in GB")
    private Integer storageCapacity;
    
    @Schema(description = "Used storage in GB")
    private Integer usedStorage;
    
    @Schema(description = "Public endpoint")
    private String publicEndpoint;
    
    @Schema(description = "Private endpoint")
    private String privateEndpoint;
    
    @Schema(description = "Number of topics")
    private Integer topicCount;
    
    @Schema(description = "Number of groups")
    private Integer groupCount;
    
    @Schema(description = "Remark")
    private String remark;
    
    @Schema(description = "Creation time")
    private LocalDateTime createTime;
    
    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
