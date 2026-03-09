package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dashboard trends data")
public class DashboardTrends {
    
    @Schema(description = "Time labels (e.g., hours or dates)", example = "[\"00:00\", \"01:00\", \"02:00\"]")
    private List<String> labels;
    
    @Schema(description = "Message publish count per time period", example = "[1000, 1200, 900]")
    private List<Long> publishCounts;
    
    @Schema(description = "Message consume count per time period", example = "[950, 1150, 880]")
    private List<Long> consumeCounts;
    
    @Schema(description = "TPS (messages per second) per time period", example = "[100.5, 120.3, 90.8]")
    private List<Double> tpsValues;
    
    @Schema(description = "Message lag per time period", example = "[50, 100, 120]")
    private List<Long> lagValues;
}
