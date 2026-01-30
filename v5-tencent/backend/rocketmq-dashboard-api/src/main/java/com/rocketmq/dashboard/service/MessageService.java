package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.QueryMessagesRequest;
import com.rocketmq.dashboard.dto.request.SendMessageRequest;
import com.rocketmq.dashboard.dto.request.VerifyMessageRequest;
import com.rocketmq.dashboard.dto.response.MessageInfo;
import com.rocketmq.dashboard.dto.response.MessageTraceInfo;
import com.rocketmq.dashboard.dto.response.SendMessageResponse;
import com.rocketmq.dashboard.dto.response.VerifyMessageResponse;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    
    public List<MessageInfo> queryMessages(QueryMessagesRequest request) throws Exception {
        log.info("Querying messages for topic: {}, key: {}, messageId: {}", 
                request.getTopicName(), request.getKey(), request.getMessageId());
        
        List<MessageInfo> messages = new ArrayList<>();
        
        // Mock data - replace with actual Tencent Cloud API calls
        for (int i = 0; i < 5; i++) {
            Map<String, String> properties = new HashMap<>();
            properties.put("ORIGIN_MESSAGE_ID", UUID.randomUUID().toString());
            properties.put("UNIQ_KEY", "order-" + (i + 1));
            
            messages.add(MessageInfo.builder()
                    .messageId("01000000000000000000000000000000" + i)
                    .topicName(request.getTopicName())
                    .queueId(i % 4)
                    .queueOffset(12345L + i)
                    .body("{\"orderId\": \"" + (123456 + i) + "\", \"amount\": " + (99.99 + i) + "}")
                    .tags("TagA")
                    .keys(request.getKey() != null ? request.getKey() : "order-" + (123456 + i))
                    .properties(properties)
                    .bodySize(128)
                    .storeTimestamp(System.currentTimeMillis() - i * 3600000)
                    .bornTimestamp(System.currentTimeMillis() - i * 3600000 - 100)
                    .bornHost("192.168.1.100:12345")
                    .storeHost("broker-a:10911")
                    .reconsumeTimes(0)
                    .storeTime(LocalDateTime.now().minusHours(i))
                    .bornTime(LocalDateTime.now().minusHours(i))
                    .build());
        }
        
        log.info("Found {} messages", messages.size());
        return messages;
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
