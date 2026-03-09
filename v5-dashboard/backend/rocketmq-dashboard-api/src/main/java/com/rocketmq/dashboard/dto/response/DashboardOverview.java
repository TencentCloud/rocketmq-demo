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
@Schema(description = "Dashboard overview statistics")
public class DashboardOverview {
    
    @Schema(description = "Total number of clusters")
    private Integer totalClusters;
    
    @Schema(description = "Total number of topics")
    private Integer totalTopics;
    
    @Schema(description = "Total number of consumer groups")
    private Integer totalGroups;
    
    @Schema(description = "Total number of messages (today)")
    private Long todayMessages;
    
    @Schema(description = "Total messages in all topics")
    private Long totalMessages;
    
    @Schema(description = "Total TPS (messages per second)")
    private Double totalTps;
    
    @Schema(description = "Total message lag")
    private Long totalLag;
    
    @Schema(description = "Number of online producers")
    private Integer onlineProducers;
    
    @Schema(description = "Number of online consumers")
    private Integer onlineConsumers;
    
    @Schema(description = "Health status: HEALTHY, WARNING, ERROR", example = "HEALTHY")
    private String healthStatus;
}
