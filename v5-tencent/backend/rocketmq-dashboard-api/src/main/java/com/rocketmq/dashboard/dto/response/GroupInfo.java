package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Consumer group information")
public class GroupInfo {
    
    @Schema(description = "Group name", example = "my-consumer-group")
    private String groupName;
    
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx")
    private String clusterId;
    
    @Schema(description = "Group description")
    private String description;
    
    @Schema(description = "Consume from where", example = "CONSUME_FROM_LAST_OFFSET")
    private String consumeFrom;
    
    @Schema(description = "Is broadcast mode")
    private Boolean broadcast;
    
    @Schema(description = "Retry enabled")
    private Boolean retryEnabled;
    
    @Schema(description = "Max retry times")
    private Integer maxRetryTimes;
    
    @Schema(description = "Number of subscribed topics")
    private Integer subscribedTopics;
    
    @Schema(description = "Number of online consumers")
    private Integer onlineConsumers;
    
    @Schema(description = "Total message lag")
    private Long totalLag;
    
    @Schema(description = "Consumption TPS")
    private Double consumeTps;
    
    @Schema(description = "Creation time")
    private LocalDateTime createTime;
    
    @Schema(description = "Last consume time")
    private LocalDateTime lastConsumeTime;
}
