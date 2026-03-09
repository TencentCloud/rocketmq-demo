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
@Schema(description = "Send message response")
public class SendMessageResponse {
    
    @Schema(description = "Message ID", example = "01000000000000000000000000000000")
    private String messageId;
    
    @Schema(description = "Queue ID", example = "2")
    private Integer queueId;
    
    @Schema(description = "Queue offset", example = "12345")
    private Long queueOffset;
    
    @Schema(description = "Send status: SUCCESS, FAILURE", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Send timestamp")
    private Long sendTimestamp;
}
