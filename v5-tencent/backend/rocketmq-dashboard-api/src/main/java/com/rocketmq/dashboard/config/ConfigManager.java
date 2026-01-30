package com.rocketmq.dashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Manager
 * Manages application configuration from multiple sources with priority:
 * 1. API dynamic settings (highest priority)
 * 2. Configuration file (~/.rocketmq-dashboard/config.json)
 * 3. Environment variables (lowest priority)
 */
@Slf4j
@Component
public class ConfigManager {

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.rocketmq-dashboard";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.json";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> configCache = new HashMap<>();

    @Value("${tencent.cloud.secret-id:}")
    private String defaultSecretId;

    @Value("${tencent.cloud.secret-key:}")
    private String defaultSecretKey;

    @Value("${tencent.cloud.region:}")
    private String defaultRegion;

    @Value("${tencent.cloud.endpoint:}")
    private String defaultEndpoint;

    @PostConstruct
    public void init() {
        try {
            ensureConfigDirectoryExists();
            loadConfigFromFile();
            log.info("ConfigManager initialized. Config file: {}", CONFIG_FILE);
        } catch (Exception e) {
            log.warn("Failed to initialize ConfigManager, using defaults", e);
        }
    }

    private void ensureConfigDirectoryExists() throws IOException {
        Path configPath = Paths.get(CONFIG_DIR);
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath);
            log.info("Created config directory: {}", CONFIG_DIR);
        }
    }

    private void loadConfigFromFile() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, String> fileConfig = objectMapper.readValue(configFile, Map.class);
                configCache.putAll(fileConfig);
                log.info("Loaded configuration from file: {} keys", fileConfig.size());
            } catch (IOException e) {
                log.warn("Failed to load config file: {}", CONFIG_FILE, e);
            }
        }
    }

    private void saveConfigToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(CONFIG_FILE), configCache);
            log.info("Configuration saved to file: {}", CONFIG_FILE);
        } catch (IOException e) {
            log.error("Failed to save config file: {}", CONFIG_FILE, e);
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        String value = configCache.get(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        value = System.getenv(key.toUpperCase().replace('.', '_'));
        if (value != null && !value.isEmpty()) {
            return value;
        }
        
        return defaultValue;
    }

    public String getSecretId() {
        return getConfigValue("tencent.cloud.secret-id", defaultSecretId);
    }

    public String getSecretKey() {
        return getConfigValue("tencent.cloud.secret-key", defaultSecretKey);
    }

    public String getRegion() {
        String region = getConfigValue("tencent.cloud.region", defaultRegion);
        return region != null && !region.isEmpty() ? region : "ap-guangzhou";
    }

    public String getEndpoint() {
        String endpoint = getConfigValue("tencent.cloud.endpoint", defaultEndpoint);
        return endpoint != null && !endpoint.isEmpty() ? endpoint : "trocket.tencentcloudapi.com";
    }

    public void setConfig(String key, String value) {
        configCache.put(key, value);
        saveConfigToFile();
        log.info("Configuration updated: {}", key);
    }

    public void setTencentCloudCredentials(String secretId, String secretKey, String region) {
        if (secretId != null && !secretId.isEmpty()) {
            setConfig("tencent.cloud.secret-id", secretId);
        }
        if (secretKey != null && !secretKey.isEmpty()) {
            setConfig("tencent.cloud.secret-key", secretKey);
        }
        if (region != null && !region.isEmpty()) {
            setConfig("tencent.cloud.region", region);
        }
        log.info("Tencent Cloud credentials updated");
    }

    public Map<String, String> getAllConfig() {
        Map<String, String> allConfig = new HashMap<>();
        allConfig.put("tencent.cloud.secret-id", maskSecretId(getSecretId()));
        allConfig.put("tencent.cloud.region", getRegion());
        allConfig.put("tencent.cloud.endpoint", getEndpoint());
        return allConfig;
    }

    private String maskSecretId(String secretId) {
        if (secretId == null || secretId.length() < 8) {
            return "****";
        }
        return secretId.substring(0, 4) + "****" + secretId.substring(secretId.length() - 4);
    }
}
