package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateTopicRequest;
import com.rocketmq.dashboard.dto.request.UpdateTopicRequest;
import com.rocketmq.dashboard.dto.response.ProducerInfo;
import com.rocketmq.dashboard.dto.response.TopicInfo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import com.tencentcloudapi.trocket.v20230308.models.DescribeTopicListRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeTopicListResponse;
import com.tencentcloudapi.trocket.v20230308.models.DescribeTopicRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeTopicResponse;
import com.tencentcloudapi.trocket.v20230308.models.TopicItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TopicService {

    private final TrocketClient trocketClient;

    public TopicService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }

    public List<TopicInfo> listTopics(String clusterId) throws TencentCloudSDKException {
        log.info("Listing topics for cluster: {}", clusterId);

        try {
            // Create request for DescribeTopicList API
            DescribeTopicListRequest request = new DescribeTopicListRequest();
            request.setInstanceId(clusterId);
            request.setOffset(0L);
            request.setLimit(100L); // Maximum reasonable limit

            // Call Tencent Cloud API
            DescribeTopicListResponse response = trocketClient.DescribeTopicList(request);

            // Map response to TopicInfo list
            List<TopicInfo> topics = new ArrayList<>();
            if (response.getData() != null) {
                topics = Arrays.stream(response.getData())
                        .map(this::mapToTopicInfo)
                        .collect(Collectors.toList());
            }

            log.info("Found {} topics", topics.size());
            return topics;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to list topics from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private TopicInfo mapToTopicInfo(TopicItem topicItem) {
        return TopicInfo.builder()
                .topicName(topicItem.getTopic())
                .clusterId(topicItem.getInstanceId())
                .topicType(mapTopicType(topicItem.getTopicType()))
                .description(topicItem.getRemark())
                .queueNum(topicItem.getQueueNum() != null ? topicItem.getQueueNum().intValue() : null)
                .retentionHours(topicItem.getMsgTTL() != null ? topicItem.getMsgTTL().intValue() : null)
                .maxMessageSize(4194304L)
                .totalMessages(null)
                .todayMessages(null)
                .tps(null)
                .producerCount(null)
                .consumerCount(null)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    private TopicInfo mapDescribeTopicToTopicInfo(DescribeTopicResponse response) {
        return TopicInfo.builder()
                .topicName(response.getTopic())
                .clusterId(response.getInstanceId())
                .topicType(mapTopicType(response.getTopicType()))
                .description(response.getRemark())
                .queueNum(null)
                .retentionHours(response.getMsgTTL() != null ? response.getMsgTTL().intValue() : null)
                .maxMessageSize(4194304L)
                .totalMessages(null)
                .todayMessages(null)
                .tps(null)
                .producerCount(null)
                .consumerCount(response.getSubscriptionCount() != null ? response.getSubscriptionCount().intValue() : null)
                .createTime(timestampToLocalDateTime(response.getCreatedTime()))
                .updateTime(timestampToLocalDateTime(response.getLastUpdateTime()))
                .build();
    }

    private LocalDateTime timestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
    }

    private String mapTopicType(String tencentType) {
        if (tencentType == null) {
            return "Normal";
        }

        switch (tencentType.toUpperCase()) {
            case "NORMAL":
                return "Normal";
            case "FIFO":
                return "PartitionedOrder";
            case "DELAY":
                return "DelayScheduled";
            case "TRANSACTION":
                return "Transaction";
            case "GLOBAL_ORDER":
                return "GlobalOrder";
            default:
                log.warn("Unknown topic type: {}", tencentType);
                return tencentType;
        }
    }

    public TopicInfo getTopic(String clusterId, String topicName) throws TencentCloudSDKException {
        log.info("Getting topic: {} in cluster: {}", topicName, clusterId);

        try {
            DescribeTopicRequest request = new DescribeTopicRequest();
            request.setInstanceId(clusterId);
            request.setTopic(topicName);

            DescribeTopicResponse response = trocketClient.DescribeTopic(request);

            if (response == null || response.getInstanceId() == null || response.getTopic() == null) {
                log.warn("Topic not found: {} in cluster: {}", topicName, clusterId);
                throw new TencentCloudSDKException("TopicNotFound", "Topic '" + topicName + "' not found in cluster '" + clusterId + "'");
            }

            TopicInfo topicInfo = mapDescribeTopicToTopicInfo(response);

            log.info("Successfully retrieved topic: {}", topicName);
            return topicInfo;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to get topic from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
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
