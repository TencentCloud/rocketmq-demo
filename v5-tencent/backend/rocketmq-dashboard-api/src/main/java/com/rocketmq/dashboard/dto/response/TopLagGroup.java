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
@Schema(description = "Top lag consumer group")
public class TopLagGroup {
    
    @Schema(description = "Group name", example = "my-consumer-group")
    private String groupName;
    
    @Schema(description = "Topic name", example = "my-topic")
    private String topicName;
    
    @Schema(description = "Total message lag")
    private Long totalLag;
    
    @Schema(description = "Consumption TPS")
    private Double consumeTps;
    
    @Schema(description = "Last consume timestamp")
    private Long lastConsumeTimestamp;
}
