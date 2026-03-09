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
@Schema(description = "Message trace information")
public class MessageTraceInfo {
    
    @Schema(description = "Trace type: Pub, SubBefore, SubAfter, EndTransaction", example = "Pub")
    private String traceType;
    
    @Schema(description = "Timestamp")
    private Long timestamp;
    
    @Schema(description = "Time")
    private LocalDateTime time;
    
    @Schema(description = "Region ID", example = "ap-guangzhou")
    private String regionId;
    
    @Schema(description = "Group name (for consumer trace)", example = "my-consumer-group")
    private String groupName;
    
    @Schema(description = "Topic name", example = "my-topic")
    private String topicName;
    
    @Schema(description = "Message ID", example = "01000000000000000000000000000000")
    private String messageId;
    
    @Schema(description = "Message keys", example = "order-123456")
    private String keys;
    
    @Schema(description = "Status: SUCCESS, FAILURE", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Store host", example = "broker-a:10911")
    private String storeHost;
    
    @Schema(description = "Client host", example = "192.168.1.100:12345")
    private String clientHost;
    
    @Schema(description = "Cost time in milliseconds")
    private Long costTime;
}
