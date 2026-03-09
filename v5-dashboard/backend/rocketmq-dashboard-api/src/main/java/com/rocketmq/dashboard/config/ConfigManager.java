package com.rocketmq.dashboard.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Configuration Manager
 * Reads Tencent Cloud credentials from application.yml.
 * Supports configuration via:
 * 1. application.yml direct values
 * 2. Environment variables: TENCENT_CLOUD_SECRET_ID, TENCENT_CLOUD_SECRET_KEY, TENCENT_CLOUD_REGION, TENCENT_CLOUD_ENDPOINT
 * 3. JVM -D parameters: -Dtencent.cloud.secret-id=xxx -Dtencent.cloud.secret-key=xxx -Dtencent.cloud.region=xxx
 */
@Slf4j
@Component
public class ConfigManager {

    @Value("${tencent.cloud.secret-id:}")
    private String secretId;

    @Value("${tencent.cloud.secret-key:}")
    private String secretKey;

    @Value("${tencent.cloud.region:ap-guangzhou}")
    private String region;

    @Value("${tencent.cloud.endpoint:trocket.tencentcloudapi.com}")
    private String endpoint;

    @PostConstruct
    public void init() {
        boolean configured = secretId != null && !secretId.isEmpty()
                && secretKey != null && !secretKey.isEmpty();
        if (configured) {
            log.info("Tencent Cloud credentials configured. Region: {}, Endpoint: {}", region, endpoint);
        } else {
            log.warn("Tencent Cloud credentials NOT configured. "
                    + "Please set via application.yml, environment variables (TENCENT_CLOUD_SECRET_ID, TENCENT_CLOUD_SECRET_KEY), "
                    + "or JVM parameters (-Dtencent.cloud.secret-id=xxx -Dtencent.cloud.secret-key=xxx)");
        }
    }

    public String getSecretId() {
        return secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region != null && !region.isEmpty() ? region : "ap-guangzhou";
    }

    public String getEndpoint() {
        return endpoint != null && !endpoint.isEmpty() ? endpoint : "trocket.tencentcloudapi.com";
    }
}
