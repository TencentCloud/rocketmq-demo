package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Region information")
public class RegionInfo {
    
    @Schema(description = "Region ID", example = "ap-guangzhou")
    private String regionId;
    
    @Schema(description = "Region name", example = "Guangzhou")
    private String regionName;
    
    @Schema(description = "Whether this region is available")
    private boolean available;
}
