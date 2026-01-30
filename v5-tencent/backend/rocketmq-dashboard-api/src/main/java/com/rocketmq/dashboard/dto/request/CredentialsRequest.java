package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Tencent Cloud credentials request")
public class CredentialsRequest {
    
    @NotBlank(message = "Secret ID is required")
    @Schema(description = "Tencent Cloud Secret ID", example = "AKIDxxxxxxxxxxxxxxxxxxxxx", required = true)
    private String secretId;
    
    @NotBlank(message = "Secret Key is required")
    @Schema(description = "Tencent Cloud Secret Key", example = "xxxxxxxxxxxxxxxxxxxxxxxx", required = true)
    private String secretKey;
    
    @Schema(description = "Region", example = "ap-guangzhou")
    private String region;
}
