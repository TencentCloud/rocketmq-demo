package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.QueryMessagesRequest;
import com.rocketmq.dashboard.dto.request.SendMessageRequest;
import com.rocketmq.dashboard.dto.request.VerifyMessageRequest;
import com.rocketmq.dashboard.dto.response.MessageInfo;
import com.rocketmq.dashboard.dto.response.MessageTraceInfo;
import com.rocketmq.dashboard.dto.response.SendMessageResponse;
import com.rocketmq.dashboard.dto.response.VerifyMessageResponse;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageListRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeMessageListResponse;
import com.tencentcloudapi.trocket.v20230308.models.MessageItem;
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

    public MessageInfo getMessageById(String clusterId, String messageId) throws Exception {
        log.info("Getting message details for messageId: {}", messageId);
        
        Map<String, String> properties = new HashMap<>();
        properties.put("ORIGIN_MESSAGE_ID", UUID.randomUUID().toString());
        properties.put("UNIQ_KEY", "order-123456");
        
        return MessageInfo.builder()
                .messageId(messageId)
                .topicName("my-topic")
                .queueId(2)
                .queueOffset(12345L)
                .body("{\"orderId\": \"123456\", \"amount\": 99.99}")
                .tags("TagA")
                .keys("order-123456")
                .properties(properties)
                .bodySize(128)
                .storeTimestamp(System.currentTimeMillis())
                .bornTimestamp(System.currentTimeMillis() - 100)
                .bornHost("192.168.1.100:12345")
                .storeHost("broker-a:10911")
                .reconsumeTimes(0)
                .storeTime(LocalDateTime.now())
                .bornTime(LocalDateTime.now())
                .build();
    }
    
    public List<MessageTraceInfo> getMessageTrace(String clusterId, String messageId) throws Exception {
        log.info("Getting message trace for messageId: {}", messageId);
        
        List<MessageTraceInfo> traces = new ArrayList<>();
        
        // Producer trace
        traces.add(MessageTraceInfo.builder()
                .traceType("Pub")
                .timestamp(System.currentTimeMillis() - 10000)
                .time(LocalDateTime.now().minusSeconds(10))
                .regionId("ap-guangzhou")
                .topicName("my-topic")
                .messageId(messageId)
                .keys("order-123456")
                .status("SUCCESS")
                .storeHost("broker-a:10911")
                .clientHost("192.168.1.100:12345")
                .costTime(5L)
                .build());
        
        // Consumer trace - before consumption
        traces.add(MessageTraceInfo.builder()
                .traceType("SubBefore")
                .timestamp(System.currentTimeMillis() - 8000)
                .time(LocalDateTime.now().minusSeconds(8))
                .regionId("ap-guangzhou")
                .groupName("my-consumer-group")
                .topicName("my-topic")
                .messageId(messageId)
                .keys("order-123456")
                .status("SUCCESS")
                .storeHost("broker-a:10911")
                .clientHost("20.157.203.89:23456")
                .costTime(2L)
                .build());
        
        // Consumer trace - after consumption
        traces.add(MessageTraceInfo.builder()
                .traceType("SubAfter")
                .timestamp(System.currentTimeMillis() - 5000)
                .time(LocalDateTime.now().minusSeconds(5))
                .regionId("ap-guangzhou")
                .groupName("my-consumer-group")
                .topicName("my-topic")
                .messageId(messageId)
                .keys("order-123456")
                .status("SUCCESS")
                .storeHost("broker-a:10911")
                .clientHost("20.157.203.89:23456")
                .costTime(150L)
                .build());
        
        log.info("Found {} trace records", traces.size());
        return traces;
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
        
        // Mock data - replace with actual Tencent Cloud API calls
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
}
