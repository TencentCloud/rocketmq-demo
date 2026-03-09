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
@Schema(description = "Verify message consumption response")
public class VerifyMessageResponse {
    
    @Schema(description = "Verification result: SUCCESS, FAILURE, NOT_CONSUMED", example = "SUCCESS")
    private String result;
    
    @Schema(description = "Consumer group name", example = "my-consumer-group")
    private String consumerGroup;
    
    @Schema(description = "Message ID", example = "01000000000000000000000000000000")
    private String messageId;
    
    @Schema(description = "Consumption status", example = "CONSUMED")
    private String consumeStatus;
    
    @Schema(description = "Consume timestamp")
    private Long consumeTimestamp;
    
    @Schema(description = "Client host", example = "192.168.1.100:12345")
    private String clientHost;
    
    @Schema(description = "Detailed message")
    private String message;
}
