package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Send message request")
public class SendMessageRequest {
    
    @NotBlank(message = "Cluster ID is required")
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx", required = true)
    private String clusterId;
    
    @NotBlank(message = "Topic name is required")
    @Schema(description = "Topic name", example = "my-topic", required = true)
    private String topicName;
    
    @NotBlank(message = "Message body is required")
    @Schema(description = "Message body", example = "{\"orderId\": \"123456\", \"amount\": 99.99}", required = true)
    private String body;
    
    @Schema(description = "Message tags", example = "TagA")
    private String tags;
    
    @Schema(description = "Message keys (for search)", example = "order-123456")
    private String keys;
    
    @Schema(description = "User properties")
    private Map<String, String> properties;
    
    @Schema(description = "Delay level (for delay messages, 0=no delay)", example = "0")
    private Integer delayLevel;
}
