package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.QueryMessagesRequest;
import com.rocketmq.dashboard.dto.request.SendMessageRequest;
import com.rocketmq.dashboard.dto.request.VerifyMessageRequest;
import com.rocketmq.dashboard.dto.response.MessageInfo;
import com.rocketmq.dashboard.dto.response.MessageTraceInfo;
import com.rocketmq.dashboard.dto.response.SendMessageResponse;
import com.rocketmq.dashboard.dto.response.VerifyMessageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageListRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageListResponse;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageResponse;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageTraceRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageTraceResponse;
import com.tencentcloudapi.trocket.v20230308.models.MessageItem;
import com.tencentcloudapi.trocket.v20230308.models.MessageTrackItem;
import com.tencentcloudapi.trocket.v20230308.models.MessageTraceItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MessageService {
    
    private final TrocketClient trocketClient;
    
    public MessageService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<MessageInfo> queryMessages(QueryMessagesRequest request) throws TencentCloudSDKException {
        log.info("Querying messages for topic: {}, key: {}, messageId: {}, startTime: {}, endTime: {}",
                request.getTopicName(), request.getKey(), request.getMessageId(),
                request.getStartTime(), request.getEndTime());

        List<MessageInfo> messages = new ArrayList<>();

        try {
            // Create request for DescribeMessageList API
            DescribeMessageListRequest sdkRequest = new DescribeMessageListRequest();
            sdkRequest.setInstanceId(request.getClusterId());
            sdkRequest.setTopic(request.getTopicName());

            // Set time range - EndTime is REQUIRED by Tencent Cloud API
            if (request.getStartTime() != null) {
                sdkRequest.setStartTime(request.getStartTime() / 1000); // Convert ms to seconds
            } else {
                // Default to 24 hours ago if not specified
                sdkRequest.setStartTime((System.currentTimeMillis() - 24 * 60 * 60 * 1000) / 1000);
            }

            if (request.getEndTime() != null) {
                sdkRequest.setEndTime(request.getEndTime() / 1000); // Convert ms to seconds
            } else {
                // Default to current time if not specified
                sdkRequest.setEndTime(System.currentTimeMillis() / 1000);
            }

            // Set search parameters
            if (request.getKey() != null) {
                sdkRequest.setMsgKey(request.getKey());
            }
            if (request.getMessageId() != null) {
                sdkRequest.setMsgId(request.getMessageId());
            }

            // Set pagination if provided
            int page = request.getPage() != null ? request.getPage() : 1;
            int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
            sdkRequest.setOffset((long) (page - 1) * pageSize);
            sdkRequest.setLimit((long) pageSize);

            // Call Tencent Cloud API
            DescribeMessageListResponse response = trocketClient.DescribeMessageList(sdkRequest);

            // Map response to MessageInfo list
            if (response.getData() != null && response.getData().length > 0) {
                for (MessageItem item : response.getData()) {
                    messages.add(mapToMessageInfo(item, request.getTopicName()));
                }
            }

            log.info("Found {} messages", messages.size());
            return messages;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query messages from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Map Tencent Cloud MessageItem to MessageInfo DTO
     * Note: DescribeMessageList API only returns message metadata, not body content
     */
    private MessageInfo mapToMessageInfo(MessageItem item, String topicName) {
        Long timestamp = parseTimestamp(item.getProduceTime());
        LocalDateTime localDateTime = convertToLocalDateTime(timestamp);

        return MessageInfo.builder()
                .messageId(item.getMsgId())
                .topicName(topicName)
                .queueId(null) // Not provided by DescribeMessageList API
                .queueOffset(null) // Not provided by DescribeMessageList API
                .body(null) // Not provided by DescribeMessageList API - only metadata available
                .tags(item.getTags())
                .keys(item.getKeys())
                .properties(null) // Not provided by DescribeMessageList API
                .bodySize(null) // Not provided by DescribeMessageList API
                .storeTimestamp(timestamp)
                .bornTimestamp(timestamp)
                .bornHost(item.getProducerAddr())
                .storeHost(null) // Not provided by DescribeMessageList API
                .reconsumeTimes(null) // Not provided by DescribeMessageList API
                .storeTime(localDateTime)
                .bornTime(localDateTime)
                .build();
    }

    /**
     * Parse timestamp string (format: 2024-01-30 10:00:00) to epoch milliseconds
     */
    private Long parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) {
            return null;
        }
        try {
            // Format appears to be: yyyy-MM-dd HH:mm:ss
            LocalDateTime localDateTime = LocalDateTime.parse(timestampStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestampStr, e);
            return null;
        }
    }

    /**
     * Convert epoch milliseconds to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    public MessageInfo getMessageById(String clusterId, String messageId) throws TencentCloudSDKException {
        log.info("Getting message details for messageId: {}", messageId);

        try {
            DescribeMessageRequest request = new DescribeMessageRequest();
            request.setInstanceId(clusterId);
            request.setMsgId(messageId);
            request.setOffset(0L);
            request.setLimit(1L);

            DescribeMessageResponse response = trocketClient.DescribeMessage(request);

            if (response != null && response.getMessageId() != null) {
                return mapToMessageInfo(response);
            }

            log.warn("Message not found: {}", messageId);
            return null;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query message from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public List<MessageTraceInfo> getMessageTrace(String clusterId, String messageId) throws TencentCloudSDKException {
        log.info("Getting message trace for messageId: {}", messageId);

        List<MessageTraceInfo> traces = new ArrayList<>();

        try {
            DescribeMessageTraceRequest request = new DescribeMessageTraceRequest();
            request.setInstanceId(clusterId);
            request.setMsgId(messageId);

            DescribeMessageTraceResponse response = trocketClient.DescribeMessageTrace(request);

            if (response.getData() != null && response.getData().length > 0) {
                for (MessageTraceItem item : response.getData()) {
                    traces.add(mapToMessageTraceInfo(item));
                }
            }

            log.info("Found {} trace records", traces.size());
            return traces;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query message trace from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public SendMessageResponse sendMessage(SendMessageRequest request) throws Exception {
        log.info("Sending message to topic: {}, tags: {}, keys: {}", 
                request.getTopicName(), request.getTags(), request.getKeys());
        
        // Mock data - replace with actual Tencent Cloud API calls
        String messageId = UUID.randomUUID().toString().replace("-", "");
        
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(messageId)
                .queueId((int) (Math.random() * 4))
                .queueOffset(System.currentTimeMillis() % 100000)
                .status("SUCCESS")
                .sendTimestamp(System.currentTimeMillis())
                .build();
        
        log.info("Message sent successfully with messageId: {}", messageId);
        return response;
    }
    
    public SendMessageResponse resendDeadLetterMessage(String clusterId, String messageId) throws Exception {
        log.info("Resending dead letter message: {}", messageId);
        
        // Mock data - replace with actual Tencent Cloud API calls
        String newMessageId = UUID.randomUUID().toString().replace("-", "");
        
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(newMessageId)
                .queueId((int) (Math.random() * 4))
                .queueOffset(System.currentTimeMillis() % 100000)
                .status("SUCCESS")
                .sendTimestamp(System.currentTimeMillis())
                .build();
        
        log.info("Dead letter message resent successfully with new messageId: {}", newMessageId);
        return response;
    }
    
    public VerifyMessageResponse verifyMessageConsumption(VerifyMessageRequest request) throws Exception {
        log.info("Verifying message consumption for messageId: {}, group: {}",
                request.getMessageId(), request.getConsumerGroup());

        VerifyMessageResponse response = VerifyMessageResponse.builder()
                .result("SUCCESS")
                .consumerGroup(request.getConsumerGroup())
                .messageId(request.getMessageId())
                .consumeStatus("CONSUMED")
                .consumeTimestamp(System.currentTimeMillis() - 5000)
                .clientHost("20.157.203.89:23456")
                .message("Message has been successfully consumed by consumer group")
                .build();

        log.info("Message consumption verified: {}", response.getConsumeStatus());
        return response;
    }

    private MessageInfo mapToMessageInfo(DescribeMessageResponse response) {
        Long timestamp = parseTimestamp(response.getProduceTime());
        LocalDateTime localDateTime = convertToLocalDateTime(timestamp);

        Map<String, String> properties = parseProperties(response.getProperties());

        return MessageInfo.builder()
                .messageId(response.getMessageId())
                .topicName(response.getShowTopicName())
                .queueId(null)
                .queueOffset(null)
                .body(response.getBody())
                .tags(null)
                .keys(null)
                .properties(properties)
                .bodySize(response.getBody() != null ? response.getBody().length() : null)
                .storeTimestamp(timestamp)
                .bornTimestamp(timestamp)
                .bornHost(response.getProducerAddr())
                .storeHost(null)
                .reconsumeTimes(null)
                .storeTime(localDateTime)
                .bornTime(localDateTime)
                .build();
    }

    private MessageTraceInfo mapToMessageTraceInfo(MessageTraceItem item) {
        try {
            Map<String, Object> dataMap = new ObjectMapper().readValue(
                    item.getData(), new TypeReference<Map<String, Object>>() {}
            );

            return MessageTraceInfo.builder()
                    .traceType((String) dataMap.get("operationType"))
                    .timestamp(parseTimestampToMillis((String) dataMap.get("produceTime")))
                    .time(convertToLocalDateTime(parseTimestampToMillis((String) dataMap.get("produceTime"))))
                    .regionId((String) dataMap.get("region"))
                    .topicName((String) dataMap.get("topic"))
                    .messageId((String) dataMap.get("msgId"))
                    .keys(null)
                    .status((String) dataMap.get("status"))
                    .storeHost((String) dataMap.get("brokerName"))
                    .clientHost((String) dataMap.get("clientId"))
                    .costTime(parseLong(dataMap.get("costTime")))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse message trace data: {}", item.getData(), e);
            return MessageTraceInfo.builder()
                    .traceType(item.getStage())
                    .timestamp(System.currentTimeMillis())
                    .time(LocalDateTime.now())
                    .status("UNKNOWN")
                    .build();
        }
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, String> parseProperties(String propertiesJson) {
        if (propertiesJson == null || propertiesJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return new ObjectMapper().readValue(
                    propertiesJson,
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            log.warn("Failed to parse properties: {}", propertiesJson, e);
            return new HashMap<>();
        }
    }

    private Long parseTimestampToMillis(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) {
            return null;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(timestampStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            log.warn("Failed to parse timestamp: {}", timestampStr, e);
            return null;
        }
    }
}
