package com.rocketmq.dashboard.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Create role request")
public class CreateRoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,64}$", message = "Role name must be 1-64 characters, alphanumeric, underscore or hyphen")
    @Schema(description = "Role name", example = "my-role", required = true)
    private String roleName;
    
    @Schema(description = "Role description", example = "Role for production access")
    private String description;
    
    @Schema(description = "Access key for this role", example = "AKID-xxxxx")
    private String accessKey;
    
    @Schema(description = "Secret key for this role", example = "SecretKey-xxxxx")
    private String secretKey;
    
    @Schema(description = "Permissions: PUB (publish), SUB (subscribe), PUB_SUB (both)", 
            example = "[\"PUB\", \"SUB\"]")
    private List<String> permissions;
    
    @Schema(description = "IP whitelist", example = "[\"192.168.1.0/24\", \"10.0.0.0/8\"]")
    private List<String> ipWhitelist;
}
