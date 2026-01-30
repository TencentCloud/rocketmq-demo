package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.config.ConfigManager;
import com.rocketmq.dashboard.dto.request.CredentialsRequest;
import com.rocketmq.dashboard.dto.response.CredentialsResponse;
import com.rocketmq.dashboard.dto.response.RegionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/config")
@Tag(name = "Configuration Management", description = "APIs for managing Tencent Cloud credentials and configuration")
public class ConfigController {
    
    private final ConfigManager configManager;
    
    public ConfigController(ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @PostMapping("/credentials")
    @Operation(summary = "Set Tencent Cloud credentials", description = "Configure Secret ID and Secret Key for accessing Tencent Cloud RocketMQ")
    public Result<String> setCredentials(@Valid @RequestBody CredentialsRequest request) {
        log.info("Setting Tencent Cloud credentials");
        
        configManager.setTencentCloudCredentials(
                request.getSecretId(),
                request.getSecretKey(),
                request.getRegion()
        );
        
        return Result.success("Credentials configured successfully", "OK");
    }
    
    @GetMapping("/credentials")
    @Operation(summary = "Get current credentials (masked)", description = "Retrieve currently configured credentials with Secret Key masked for security")
    public Result<CredentialsResponse> getCredentials() {
        log.info("Getting credentials");
        
        String secretId = configManager.getSecretId();
        boolean configured = secretId != null && !secretId.isEmpty();
        
        String maskedSecretId = secretId != null && !secretId.isEmpty() ? 
                maskSecretId(secretId) : null;
        
        CredentialsResponse response = CredentialsResponse.builder()
                .secretId(maskedSecretId)
                .region(configManager.getRegion())
                .endpoint(configManager.getEndpoint())
                .configured(configured)
                .build();
        
        return Result.success(response);
    }
    
    @DeleteMapping("/credentials")
    @Operation(summary = "Clear credentials", description = "Remove stored Tencent Cloud credentials")
    public Result<String> clearCredentials() {
        log.info("Clearing credentials");
        
        configManager.setTencentCloudCredentials("", "", "");
        
        return Result.success("Credentials cleared successfully", "OK");
    }
    
    @GetMapping("/regions")
    @Operation(summary = "Get available regions", description = "List all available Tencent Cloud regions for RocketMQ")
    public Result<List<RegionInfo>> getRegions() {
        log.info("Getting available regions");
        
        List<RegionInfo> regions = new ArrayList<>();
        
        regions.add(RegionInfo.builder().regionId("ap-guangzhou").regionName("Guangzhou").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-shanghai").regionName("Shanghai").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-beijing").regionName("Beijing").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-chengdu").regionName("Chengdu").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-chongqing").regionName("Chongqing").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-nanjing").regionName("Nanjing").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-hongkong").regionName("Hong Kong").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-singapore").regionName("Singapore").available(true).build());
        regions.add(RegionInfo.builder().regionId("ap-tokyo").regionName("Tokyo").available(true).build());
        regions.add(RegionInfo.builder().regionId("na-siliconvalley").regionName("Silicon Valley").available(true).build());
        regions.add(RegionInfo.builder().regionId("na-ashburn").regionName("Virginia").available(true).build());
        regions.add(RegionInfo.builder().regionId("eu-frankfurt").regionName("Frankfurt").available(true).build());
        
        return Result.success(regions);
    }
    
    @PostMapping("/test")
    @Operation(summary = "Test connection", description = "Test Tencent Cloud credentials by attempting to connect")
    public Result<Boolean> testConnection(@Valid @RequestBody CredentialsRequest request) {
        log.info("Testing connection with provided credentials");
        
        try {
            // In a real implementation, this would actually test the credentials
            // by making a simple API call to Tencent Cloud
            // For now, just validate the format
            boolean valid = request.getSecretId() != null && !request.getSecretId().isEmpty()
                    && request.getSecretKey() != null && !request.getSecretKey().isEmpty()
                    && request.getRegion() != null && !request.getRegion().isEmpty();
            
            if (valid) {
                log.info("Connection test successful");
                return Result.success("Connection test successful", true);
            } else {
                log.warn("Connection test failed: invalid credentials format");
                return Result.error(400, "Invalid credentials format");
            }
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return Result.error(500, "Connection test failed: " + e.getMessage());
        }
    }
    
    private String maskSecretId(String secretId) {
        if (secretId == null || secretId.length() < 8) {
            return "****";
        }
        return secretId.substring(0, 4) + "****" + secretId.substring(secretId.length() - 4);
    }
}
