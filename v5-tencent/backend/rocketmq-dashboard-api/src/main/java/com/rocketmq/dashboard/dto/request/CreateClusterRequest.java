package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Create cluster request")
public class CreateClusterRequest {
    
    @NotBlank(message = "Cluster name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$", message = "Cluster name must be 1-64 characters, alphanumeric, underscore or hyphen")
    @Schema(description = "Cluster name", example = "my-rocketmq-cluster", required = true)
    private String clusterName;
    
    @Schema(description = "Cluster description", example = "Production RocketMQ cluster")
    private String description;
    
    @NotBlank(message = "Region is required")
    @Schema(description = "Region", example = "ap-guangzhou", required = true)
    private String region;
    
    @Schema(description = "VPC ID", example = "vpc-xxxxx")
    private String vpcId;
    
    @Schema(description = "Subnet ID", example = "subnet-xxxxx")
    private String subnetId;
    
    @Schema(description = "Cluster type: 4.x or 5.x", example = "5.x", defaultValue = "5.x")
    private String clusterType;
    
    @Schema(description = "Maximum TPS", example = "10000", defaultValue = "10000")
    private Integer maxTps;
    
    @Schema(description = "Maximum bandwidth in MB/s", example = "100", defaultValue = "100")
    private Integer maxBandwidth;
    
    @Schema(description = "Storage capacity in GB", example = "500", defaultValue = "500")
    private Integer storageCapacity;
}
