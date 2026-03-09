package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Consumer lag information")
public class ConsumerLagInfo {
    
    @Schema(description = "Topic name", example = "my-topic")
    private String topicName;
    
    @Schema(description = "Queue ID", example = "0")
    private Integer queueId;
    
    @Schema(description = "Broker offset (total messages)")
    private Long brokerOffset;
    
    @Schema(description = "Consumer offset (consumed messages)")
    private Long consumerOffset;
    
    @Schema(description = "Message lag (unconsumed messages)")
    private Long lag;
    
    @Schema(description = "Last consume timestamp")
    private Long lastConsumeTimestamp;
}
