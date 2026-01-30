package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Update cluster request")
public class UpdateClusterRequest {
    
    @Schema(description = "Cluster description", example = "Updated production cluster description")
    private String description;
    
    @Schema(description = "Maximum TPS", example = "20000")
    private Integer maxTps;
    
    @Schema(description = "Maximum bandwidth in MB/s", example = "200")
    private Integer maxBandwidth;
    
    @Schema(description = "Storage capacity in GB", example = "1000")
    private Integer storageCapacity;
}
