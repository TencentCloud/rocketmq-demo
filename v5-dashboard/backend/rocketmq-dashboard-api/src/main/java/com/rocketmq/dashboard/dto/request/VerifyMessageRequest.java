package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Verify message consumption request")
public class VerifyMessageRequest {
    
    @NotBlank(message = "Cluster ID is required")
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx", required = true)
    private String clusterId;
    
    @NotBlank(message = "Topic name is required")
    @Schema(description = "Topic name", example = "my-topic", required = true)
    private String topicName;
    
    @NotBlank(message = "Consumer group is required")
    @Schema(description = "Consumer group name", example = "my-consumer-group", required = true)
    private String consumerGroup;
    
    @NotBlank(message = "Message ID is required")
    @Schema(description = "Message ID", example = "01000000000000000000000000000000", required = true)
    private String messageId;
}
