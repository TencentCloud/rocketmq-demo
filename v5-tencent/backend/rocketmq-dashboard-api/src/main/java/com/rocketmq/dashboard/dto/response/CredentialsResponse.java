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
@Schema(description = "Credentials response (masked)")
public class CredentialsResponse {
    
    @Schema(description = "Masked Secret ID", example = "AKID****xxxx")
    private String secretId;
    
    @Schema(description = "Region", example = "ap-guangzhou")
    private String region;
    
    @Schema(description = "Endpoint", example = "trocket.tencentcloudapi.com")
    private String endpoint;
    
    @Schema(description = "Whether credentials are configured")
    private boolean configured;
}
