package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Reset consumer offset request")
public class ResetOffsetRequest {
    
    @NotBlank(message = "Topic name is required")
    @Schema(description = "Topic name", example = "my-topic", required = true)
    private String topicName;
    
    @Schema(description = "Reset type: LATEST, EARLIEST, TIMESTAMP", example = "LATEST", required = true)
    private String resetType;
    
    @Schema(description = "Timestamp for TIMESTAMP reset type", example = "1704038400000")
    private Long timestamp;
}
