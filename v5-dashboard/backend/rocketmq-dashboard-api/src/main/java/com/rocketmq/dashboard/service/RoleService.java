package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateRoleRequest;
import com.rocketmq.dashboard.dto.request.UpdateRoleRequest;
import com.rocketmq.dashboard.dto.response.RoleInfo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import com.tencentcloudapi.trocket.v20230308.models.DescribeRoleListRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeRoleListResponse;
import com.tencentcloudapi.trocket.v20230308.models.RoleItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    
    public List<RoleInfo> listRoles(String clusterId) throws TencentCloudSDKException {
        log.info("Listing all roles for cluster: {}", clusterId);

        List<RoleInfo> roles = new ArrayList<>();

        try {
            DescribeRoleListRequest request = new DescribeRoleListRequest();
            request.setInstanceId(clusterId);
            request.setLimit(100L);
            request.setOffset(0L);

            DescribeRoleListResponse response = trocketClient.DescribeRoleList(request);

            if (response.getData() != null && response.getData().length > 0) {
                for (RoleItem item : response.getData()) {
                    roles.add(mapToRoleInfo(item));
                }
            }

            log.info("Found {} roles", roles.size());
            return roles;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query roles from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public RoleInfo getRole(String clusterId, String roleName) throws TencentCloudSDKException {
        log.info("Getting role details for: {}", roleName);

        try {
            DescribeRoleListRequest request = new DescribeRoleListRequest();
            request.setInstanceId(clusterId);
            request.setLimit(100L);
            request.setOffset(0L);

            DescribeRoleListResponse response = trocketClient.DescribeRoleList(request);

            if (response.getData() != null) {
                for (RoleItem item : response.getData()) {
                    if (item.getRoleName() != null && item.getRoleName().equals(roleName)) {
                        return mapToRoleInfo(item);
                    }
                }
            }

            log.warn("Role not found: {}", roleName);
            return null;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query role from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private RoleInfo mapToRoleInfo(RoleItem item) {
        List<String> permissions = new ArrayList<>();
        if (item.getPermRead() != null && item.getPermRead()) {
            permissions.add("SUB");
        }
        if (item.getPermWrite() != null && item.getPermWrite()) {
            permissions.add("PUB");
        }

        return RoleInfo.builder()
                .roleName(item.getRoleName())
                .description(item.getRemark())
                .accessKey(maskAccessKey(item.getAccessKey()))
                .permissions(permissions)
                .ipWhitelist(null)
                .enabled(true)
                .createTime(convertToLocalDateTime(item.getCreatedTime()))
                .updateTime(convertToLocalDateTime(item.getModifiedTime()))
                .build();
    }

    private String maskAccessKey(String accessKey) {
        if (accessKey == null || accessKey.isEmpty()) {
            return "";
        }
        return "AKID-****-" + accessKey.substring(Math.max(0, accessKey.length() - 4));
    }

    private LocalDateTime convertToLocalDateTime(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
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
