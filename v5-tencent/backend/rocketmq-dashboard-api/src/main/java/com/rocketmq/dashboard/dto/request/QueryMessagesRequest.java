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

    @Schema(description = "Query type: BY_ID / BY_TIME / RECENT", example = "BY_ID")
    private String queryType;

    @Schema(description = "Message ID for exact match", example = "01000000000000000000000000000000")
    private String messageId;

    @Schema(description = "Message key for search", example = "order-123456")
    private String msgKey;

    @Schema(description = "Message tag for search", example = "tag1")
    private String tag;

    @Schema(description = "Start time (timestamp in milliseconds)", example = "1704038400000")
    private Long startTime;

    @Schema(description = "End time (timestamp in milliseconds)", example = "1704124800000")
    private Long endTime;

    @Schema(description = "Query recent N messages (max 1024), used when queryType=RECENT", example = "100")
    private Integer recentNum;

    @Schema(description = "Page offset", example = "0", defaultValue = "0")
    private Integer offset;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    private Integer limit;
}
