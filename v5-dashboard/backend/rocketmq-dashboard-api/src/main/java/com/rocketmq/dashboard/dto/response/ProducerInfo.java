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
@Schema(description = "Producer information")
public class ProducerInfo {
    
    @Schema(description = "Producer ID", example = "producer-001")
    private String producerId;
    
    @Schema(description = "Client ID", example = "client-192.168.1.100@12345")
    private String clientId;
    
    @Schema(description = "Client address", example = "192.168.1.100:12345")
    private String clientAddress;
    
    @Schema(description = "Connection status: ONLINE, OFFLINE", example = "ONLINE")
    private String status;
    
    @Schema(description = "Client version", example = "5.1.0")
    private String version;
    
    @Schema(description = "Last message send time")
    private LocalDateTime lastSendTime;
    
    @Schema(description = "Total messages sent")
    private Long totalMessagesSent;
    
    @Schema(description = "Connection time")
    private LocalDateTime connectionTime;
}
