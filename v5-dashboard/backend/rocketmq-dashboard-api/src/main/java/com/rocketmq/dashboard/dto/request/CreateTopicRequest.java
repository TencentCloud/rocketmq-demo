package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Create topic request")
public class CreateTopicRequest {
    
    @NotBlank(message = "Cluster ID is required")
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx", required = true)
    private String clusterId;
    
    @NotBlank(message = "Topic name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$", message = "Topic name must be 1-64 characters, alphanumeric, underscore or hyphen")
    @Schema(description = "Topic name", example = "my-topic", required = true)
    private String topicName;
    
    @Schema(description = "Topic type: Normal, PartitionedOrder, GlobalOrder, Transaction, DelayScheduled", 
            example = "Normal", defaultValue = "Normal")
    private String topicType;
    
    @Schema(description = "Topic description", example = "User order topic")
    private String description;
    
    @Min(value = 1, message = "Queue number must be at least 1")
    @Schema(description = "Number of queues", example = "8", defaultValue = "8")
    private Integer queueNum;
    
    @Schema(description = "Message retention hours", example = "72", defaultValue = "72")
    private Integer retentionHours;
    
    @Schema(description = "Maximum message size in bytes", example = "4194304", defaultValue = "4194304")
    private Long maxMessageSize;
}
