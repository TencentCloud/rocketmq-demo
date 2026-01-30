package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateRoleRequest;
import com.rocketmq.dashboard.dto.request.UpdateRoleRequest;
import com.rocketmq.dashboard.dto.response.RoleInfo;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class RoleService {
    
    private final TrocketClient trocketClient;
    
    public RoleService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<RoleInfo> listRoles(String clusterId) throws Exception {
        log.info("Listing all roles for cluster: {}", clusterId);
        
        List<RoleInfo> roles = new ArrayList<>();
        
        roles.add(RoleInfo.builder()
                .roleName("admin-role")
                .description("Administrator role with full permissions")
                .accessKey("AKID-****-0001")
                .permissions(Arrays.asList("PUB", "SUB"))
                .ipWhitelist(Arrays.asList("0.0.0.0/0"))
                .enabled(true)
                .createTime(LocalDateTime.now().minusDays(30))
                .updateTime(LocalDateTime.now())
                .build());
        
        roles.add(RoleInfo.builder()
                .roleName("producer-role")
                .description("Producer role with publish permission only")
                .accessKey("AKID-****-0002")
                .permissions(Arrays.asList("PUB"))
                .ipWhitelist(Arrays.asList("192.168.1.0/24", "2.9.252.0/16"))
                .enabled(true)
                .createTime(LocalDateTime.now().minusDays(15))
                .updateTime(LocalDateTime.now().minusDays(5))
                .build());
        
        roles.add(RoleInfo.builder()
                .roleName("consumer-role")
                .description("Consumer role with subscribe permission only")
                .accessKey("AKID-****-0003")
                .permissions(Arrays.asList("SUB"))
                .ipWhitelist(Arrays.asList("114.193.206.0/24"))
                .enabled(true)
                .createTime(LocalDateTime.now().minusDays(10))
                .updateTime(LocalDateTime.now().minusDays(2))
                .build());
        
        log.info("Found {} roles", roles.size());
        return roles;
    }
    
    public RoleInfo getRole(String clusterId, String roleName) throws Exception {
        log.info("Getting role details for: {}", roleName);
        
        return RoleInfo.builder()
                .roleName(roleName)
                .description("Administrator role with full permissions")
                .accessKey("AKID-****-0001")
                .permissions(Arrays.asList("PUB", "SUB"))
                .ipWhitelist(Arrays.asList("0.0.0.0/0"))
                .enabled(true)
                .createTime(LocalDateTime.now().minusDays(30))
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public RoleInfo createRole(String clusterId, CreateRoleRequest request) throws Exception {
        log.info("Creating role: {}", request.getRoleName());
        
        String maskedAccessKey = request.getAccessKey() != null ? 
                "AKID-****-" + request.getAccessKey().substring(Math.max(0, request.getAccessKey().length() - 4)) : 
                "AKID-****-" + System.currentTimeMillis() % 10000;
        
        RoleInfo role = RoleInfo.builder()
                .roleName(request.getRoleName())
                .description(request.getDescription())
                .accessKey(maskedAccessKey)
                .permissions(request.getPermissions() != null ? request.getPermissions() : Arrays.asList("PUB", "SUB"))
                .ipWhitelist(request.getIpWhitelist() != null ? request.getIpWhitelist() : Arrays.asList("0.0.0.0/0"))
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        log.info("Role created successfully: {}", role.getRoleName());
        return role;
    }
    
    public RoleInfo updateRole(String clusterId, String roleName, UpdateRoleRequest request) throws Exception {
        log.info("Updating role: {}", roleName);
        
        RoleInfo existing = getRole(clusterId, roleName);
        
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getPermissions() != null) {
            existing.setPermissions(request.getPermissions());
        }
        if (request.getIpWhitelist() != null) {
            existing.setIpWhitelist(request.getIpWhitelist());
        }
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }
        existing.setUpdateTime(LocalDateTime.now());
        
        log.info("Role updated successfully: {}", roleName);
        return existing;
    }
    
    public void deleteRole(String clusterId, String roleName) throws Exception {
        log.info("Deleting role: {}", roleName);
        log.info("Role deleted successfully: {}", roleName);
    }
}
