package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Create consumer group request")
public class CreateGroupRequest {
    
    @NotBlank(message = "Cluster ID is required")
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx", required = true)
    private String clusterId;
    
    @NotBlank(message = "Group name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$", message = "Group name must be 1-64 characters, alphanumeric, underscore or hyphen")
    @Schema(description = "Group name", example = "my-consumer-group", required = true)
    private String groupName;
    
    @Schema(description = "Group description", example = "Order processing consumer group")
    private String description;
    
    @Schema(description = "Consume from where: CONSUME_FROM_LAST_OFFSET, CONSUME_FROM_FIRST_OFFSET, CONSUME_FROM_TIMESTAMP", 
            example = "CONSUME_FROM_LAST_OFFSET", defaultValue = "CONSUME_FROM_LAST_OFFSET")
    private String consumeFrom;
    
    @Schema(description = "Enable broadcast mode", example = "false", defaultValue = "false")
    private Boolean broadcast;
    
    @Schema(description = "Enable retry", example = "true", defaultValue = "true")
    private Boolean retryEnabled;
    
    @Schema(description = "Max retry times", example = "16", defaultValue = "16")
    private Integer maxRetryTimes;
}
