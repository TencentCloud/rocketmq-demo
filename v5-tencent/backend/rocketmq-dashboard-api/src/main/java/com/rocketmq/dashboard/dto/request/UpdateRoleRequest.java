package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Update role request")
public class UpdateRoleRequest {
    
    @Schema(description = "Role description")
    private String description;
    
    @Schema(description = "Permissions: PUB (publish), SUB (subscribe), PUB_SUB (both)", 
            example = "[\"PUB\", \"SUB\"]")
    private List<String> permissions;
    
    @Schema(description = "IP whitelist", example = "[\"192.168.1.0/24\", \"10.0.0.0/8\"]")
    private List<String> ipWhitelist;
    
    @Schema(description = "Enable or disable role", example = "true")
    private Boolean enabled;
}
