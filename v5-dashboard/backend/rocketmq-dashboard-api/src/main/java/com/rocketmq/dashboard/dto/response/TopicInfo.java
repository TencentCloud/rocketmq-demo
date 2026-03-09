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
@Schema(description = "Topic information")
public class TopicInfo {
    
    @Schema(description = "Topic name", example = "my-topic")
    private String topicName;
    
    @Schema(description = "Cluster ID", example = "rmq-cn-xxxxx")
    private String clusterId;
    
    @Schema(description = "Topic type: Normal, PartitionedOrder, GlobalOrder, Transaction, DelayScheduled", example = "Normal")
    private String topicType;
    
    @Schema(description = "Topic description")
    private String description;
    
    @Schema(description = "Number of queues")
    private Integer queueNum;
    
    @Schema(description = "Message retention hours")
    private Integer retentionHours;
    
    @Schema(description = "Maximum message size in bytes")
    private Long maxMessageSize;
    
    @Schema(description = "Total messages")
    private Long totalMessages;
    
    @Schema(description = "Today's messages")
    private Long todayMessages;
    
    @Schema(description = "TPS (messages per second)")
    private Double tps;
    
    @Schema(description = "Number of producers")
    private Integer producerCount;
    
    @Schema(description = "Number of consumers")
    private Integer consumerCount;
    
    @Schema(description = "Creation time")
    private LocalDateTime createTime;
    
    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
