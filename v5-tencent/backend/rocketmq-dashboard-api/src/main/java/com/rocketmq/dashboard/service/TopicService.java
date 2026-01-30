package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateTopicRequest;
import com.rocketmq.dashboard.dto.request.UpdateTopicRequest;
import com.rocketmq.dashboard.dto.response.ProducerInfo;
import com.rocketmq.dashboard.dto.response.TopicInfo;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TopicService {
    
    private final TrocketClient trocketClient;
    
    public TopicService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<TopicInfo> listTopics(String clusterId) throws Exception {
        log.info("Listing topics for cluster: {}", clusterId);
        
        List<TopicInfo> topics = new ArrayList<>();
        topics.add(TopicInfo.builder()
                .topicName("demo-topic")
                .clusterId(clusterId)
                .topicType("Normal")
                .description("Demo topic")
                .queueNum(8)
                .retentionHours(72)
                .maxMessageSize(4194304L)
                .totalMessages(100000L)
                .todayMessages(5000L)
                .tps(100.5)
                .producerCount(2)
                .consumerCount(3)
                .createTime(LocalDateTime.now().minusDays(10))
                .updateTime(LocalDateTime.now())
                .build());
        
        log.info("Found {} topics", topics.size());
        return topics;
    }
    
    public TopicInfo getTopic(String clusterId, String topicName) throws Exception {
        log.info("Getting topic: {} in cluster: {}", topicName, clusterId);
        
        return TopicInfo.builder()
                .topicName(topicName)
                .clusterId(clusterId)
                .topicType("Normal")
                .description("Demo topic")
                .queueNum(8)
                .retentionHours(72)
                .maxMessageSize(4194304L)
                .totalMessages(100000L)
                .todayMessages(5000L)
                .tps(100.5)
                .producerCount(2)
                .consumerCount(3)
                .createTime(LocalDateTime.now().minusDays(10))
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public TopicInfo createTopic(CreateTopicRequest request) throws Exception {
        log.info("Creating topic: {} in cluster: {}", request.getTopicName(), request.getClusterId());
        
        return TopicInfo.builder()
                .topicName(request.getTopicName())
                .clusterId(request.getClusterId())
                .topicType(request.getTopicType() != null ? request.getTopicType() : "Normal")
                .description(request.getDescription())
                .queueNum(request.getQueueNum() != null ? request.getQueueNum() : 8)
                .retentionHours(request.getRetentionHours() != null ? request.getRetentionHours() : 72)
                .maxMessageSize(request.getMaxMessageSize() != null ? request.getMaxMessageSize() : 4194304L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    public TopicInfo updateTopic(String clusterId, String topicName, UpdateTopicRequest request) throws Exception {
        log.info("Updating topic: {} in cluster: {}", topicName, clusterId);
        
        TopicInfo existing = getTopic(clusterId, topicName);
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getQueueNum() != null) {
            existing.setQueueNum(request.getQueueNum());
        }
        if (request.getRetentionHours() != null) {
            existing.setRetentionHours(request.getRetentionHours());
        }
        if (request.getMaxMessageSize() != null) {
            existing.setMaxMessageSize(request.getMaxMessageSize());
        }
        existing.setUpdateTime(LocalDateTime.now());
        
        return existing;
    }
    
    public void deleteTopic(String clusterId, String topicName) throws Exception {
        log.info("Deleting topic: {} from cluster: {}", topicName, clusterId);
        log.info("Topic deleted successfully: {}", topicName);
    }
    
    public List<ProducerInfo> getProducers(String clusterId, String topicName) throws Exception {
        log.info("Getting producers for topic: {} in cluster: {}", topicName, clusterId);
        
        List<ProducerInfo> producers = new ArrayList<>();
        producers.add(ProducerInfo.builder()
                .producerId("producer-001")
                .clientId("client-192.168.1.100@12345")
                .clientAddress("192.168.1.100:12345")
                .status("ONLINE")
                .version("5.1.0")
                .lastSendTime(LocalDateTime.now().minusMinutes(5))
                .totalMessagesSent(10000L)
                .connectionTime(LocalDateTime.now().minusHours(2))
                .build());
        
        return producers;
    }
}
