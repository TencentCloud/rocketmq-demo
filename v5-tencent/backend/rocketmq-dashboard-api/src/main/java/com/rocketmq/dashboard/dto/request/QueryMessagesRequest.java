package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Query messages request")
public class QueryMessagesRequest {
    
    @NotBlank(message = "Cluster ID is required")
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx", required = true)
    private String clusterId;
    
    @NotBlank(message = "Topic name is required")
    @Schema(description = "Topic name", example = "my-topic", required = true)
    private String topicName;
    
    @Schema(description = "Message key for search", example = "order-123456")
    private String key;
    
    @Schema(description = "Message ID for exact match", example = "01000000000000000000000000000000")
    private String messageId;
    
    @Schema(description = "Start time (timestamp in milliseconds)", example = "1704038400000")
    private Long startTime;
    
    @Schema(description = "End time (timestamp in milliseconds)", example = "1704124800000")
    private Long endTime;
    
    @Schema(description = "Page number", example = "1", defaultValue = "1")
    private Integer page;
    
    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer pageSize;
}
