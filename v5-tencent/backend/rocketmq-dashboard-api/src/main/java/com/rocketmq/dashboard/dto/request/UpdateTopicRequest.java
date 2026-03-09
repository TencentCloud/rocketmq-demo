package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "Update topic request")
public class UpdateTopicRequest {
    
    @Schema(description = "Topic description")
    private String description;
    
    @Min(value = 1, message = "Queue number must be at least 1")
    @Schema(description = "Number of queues", example = "16")
    private Integer queueNum;
    
    @Schema(description = "Message retention hours", example = "168")
    private Integer retentionHours;
    
    @Schema(description = "Maximum message size in bytes", example = "8388608")
    private Long maxMessageSize;
}
