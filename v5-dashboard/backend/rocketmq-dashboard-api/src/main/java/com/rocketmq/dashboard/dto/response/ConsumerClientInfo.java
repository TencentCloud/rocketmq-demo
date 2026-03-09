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
@Schema(description = "Consumer client information")
public class ConsumerClientInfo {
    
    @Schema(description = "Client ID", example = "client-192.168.1.100@12345")
    private String clientId;
    
    @Schema(description = "Client address", example = "192.168.1.100:12345")
    private String clientAddress;
    
    @Schema(description = "Client language", example = "JAVA")
    private String language;
    
    @Schema(description = "Client version", example = "5.1.0")
    private String version;
    
    @Schema(description = "Connection status: ONLINE, OFFLINE", example = "ONLINE")
    private String status;
    
    @Schema(description = "Subscribed topics")
    private String subscribedTopics;
    
    @Schema(description = "Connection time")
    private LocalDateTime connectionTime;
    
    @Schema(description = "Last heartbeat time")
    private LocalDateTime lastHeartbeat;
}
