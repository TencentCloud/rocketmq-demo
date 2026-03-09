package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message information")
public class MessageInfo {
    
    @Schema(description = "Message ID", example = "01000000000000000000000000000000")
    private String messageId;
    
    @Schema(description = "Topic name", example = "my-topic")
    private String topicName;
    
    @Schema(description = "Queue ID", example = "2")
    private Integer queueId;
    
    @Schema(description = "Queue offset", example = "12345")
    private Long queueOffset;
    
    @Schema(description = "Message body")
    private String body;
    
    @Schema(description = "Message tags", example = "TagA")
    private String tags;
    
    @Schema(description = "Message keys", example = "order-123456")
    private String keys;
    
    @Schema(description = "User properties")
    private Map<String, String> properties;
    
    @Schema(description = "Body size in bytes")
    private Integer bodySize;
    
    @Schema(description = "Store timestamp")
    private Long storeTimestamp;
    
    @Schema(description = "Born timestamp")
    private Long bornTimestamp;
    
    @Schema(description = "Born host", example = "192.168.1.100:12345")
    private String bornHost;
    
    @Schema(description = "Store host", example = "broker-a:10911")
    private String storeHost;
    
    @Schema(description = "Reconsume times")
    private Integer reconsumeTimes;
    
    @Schema(description = "Store time")
    private LocalDateTime storeTime;
    
    @Schema(description = "Born time")
    private LocalDateTime bornTime;
}
