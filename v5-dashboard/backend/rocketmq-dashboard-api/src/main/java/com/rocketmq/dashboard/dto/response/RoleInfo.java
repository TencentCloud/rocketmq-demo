package com.rocketmq.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role information")
public class RoleInfo {
    
    @Schema(description = "Role name", example = "my-role")
    private String roleName;
    
    @Schema(description = "Role description")
    private String description;
    
    @Schema(description = "Access key (masked)", example = "AKID-****-xxxx")
    private String accessKey;
    
    @Schema(description = "Permissions")
    private List<String> permissions;
    
    @Schema(description = "IP whitelist")
    private List<String> ipWhitelist;
    
    @Schema(description = "Enabled status")
    private Boolean enabled;
    
    @Schema(description = "Creation time")
    private LocalDateTime createTime;
    
    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
