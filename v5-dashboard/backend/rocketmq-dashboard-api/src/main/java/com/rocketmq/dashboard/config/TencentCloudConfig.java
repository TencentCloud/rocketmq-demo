package com.rocketmq.dashboard.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tencent Cloud Configuration
 * Initializes Tencent Cloud RocketMQ SDK client
 */
@Slf4j
@Configuration
public class TencentCloudConfig {

    /**
     * Create Tencent Cloud RocketMQ client
     * Credentials will be loaded from ConfigManager
     */
    @Bean
    public TrocketClient trocketClient(ConfigManager configManager) {
        try {
            String secretId = configManager.getSecretId();
            String secretKey = configManager.getSecretKey();
            String region = configManager.getRegion();
            String endpoint = configManager.getEndpoint();

            if (secretId == null || secretKey == null) {
                log.warn("Tencent Cloud credentials not configured. SDK client will be created but may not work until configured.");
                secretId = "";
                secretKey = "";
            }

            // Create credential
            Credential credential = new Credential(secretId, secretKey);

            // Configure HTTP profile
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(endpoint != null ? endpoint : "trocket.tencentcloudapi.com");
            httpProfile.setReqMethod("POST");
            httpProfile.setConnTimeout(60);
            httpProfile.setReadTimeout(60);

            // Configure client profile
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            // Create client
            TrocketClient client = new TrocketClient(credential, region != null ? region : "", clientProfile);
            
            log.info("Tencent Cloud RocketMQ SDK client initialized successfully. Region: {}, Endpoint: {}", 
                    region, httpProfile.getEndpoint());
            
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize Tencent Cloud RocketMQ SDK client", e);
            throw new RuntimeException("Failed to initialize Tencent Cloud client", e);
        }
    }
}
