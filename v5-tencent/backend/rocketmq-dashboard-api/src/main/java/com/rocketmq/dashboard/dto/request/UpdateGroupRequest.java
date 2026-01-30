package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Update consumer group request")
public class UpdateGroupRequest {
    
    @Schema(description = "Group description")
    private String description;
    
    @Schema(description = "Enable retry", example = "true")
    private Boolean retryEnabled;
    
    @Schema(description = "Max retry times", example = "32")
    private Integer maxRetryTimes;
}
