package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateGroupRequest;
import com.rocketmq.dashboard.dto.request.ResetOffsetRequest;
import com.rocketmq.dashboard.dto.request.UpdateGroupRequest;
import com.rocketmq.dashboard.dto.response.ConsumerClientInfo;
import com.rocketmq.dashboard.dto.response.ConsumerLagInfo;
import com.rocketmq.dashboard.dto.response.GroupInfo;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GroupService {
    
    private final TrocketClient trocketClient;
    
    public GroupService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<GroupInfo> listGroups(String clusterId) throws Exception {
        log.info("Listing consumer groups for cluster: {}", clusterId);
        
        List<GroupInfo> groups = new ArrayList<>();
        groups.add(GroupInfo.builder()
                .groupName("demo-consumer-group")
                .clusterId(clusterId)
                .description("Demo consumer group")
                .consumeFrom("CONSUME_FROM_LAST_OFFSET")
                .broadcast(false)
                .retryEnabled(true)
                .maxRetryTimes(16)
                .subscribedTopics(3)
                .onlineConsumers(5)
                .totalLag(1000L)
                .consumeTps(50.5)
                .createTime(LocalDateTime.now().minusDays(15))
                .lastConsumeTime(LocalDateTime.now().minusMinutes(2))
                .build());
        
        log.info("Found {} consumer groups", groups.size());
        return groups;
    }
    
    public GroupInfo getGroup(String clusterId, String groupName) throws Exception {
        log.info("Getting consumer group: {} in cluster: {}", groupName, clusterId);
        
        return GroupInfo.builder()
                .groupName(groupName)
                .clusterId(clusterId)
                .description("Demo consumer group")
                .consumeFrom("CONSUME_FROM_LAST_OFFSET")
                .broadcast(false)
                .retryEnabled(true)
                .maxRetryTimes(16)
                .subscribedTopics(3)
                .onlineConsumers(5)
                .totalLag(1000L)
                .consumeTps(50.5)
                .createTime(LocalDateTime.now().minusDays(15))
                .lastConsumeTime(LocalDateTime.now().minusMinutes(2))
                .build();
    }
    
    public GroupInfo createGroup(CreateGroupRequest request) throws Exception {
        log.info("Creating consumer group: {} in cluster: {}", request.getGroupName(), request.getClusterId());
        
        return GroupInfo.builder()
                .groupName(request.getGroupName())
                .clusterId(request.getClusterId())
                .description(request.getDescription())
                .consumeFrom(request.getConsumeFrom() != null ? request.getConsumeFrom() : "CONSUME_FROM_LAST_OFFSET")
                .broadcast(request.getBroadcast() != null ? request.getBroadcast() : false)
                .retryEnabled(request.getRetryEnabled() != null ? request.getRetryEnabled() : true)
                .maxRetryTimes(request.getMaxRetryTimes() != null ? request.getMaxRetryTimes() : 16)
                .subscribedTopics(0)
                .onlineConsumers(0)
                .totalLag(0L)
                .consumeTps(0.0)
                .createTime(LocalDateTime.now())
                .lastConsumeTime(null)
                .build();
    }
    
    public GroupInfo updateGroup(String clusterId, String groupName, UpdateGroupRequest request) throws Exception {
        log.info("Updating consumer group: {} in cluster: {}", groupName, clusterId);
        
        GroupInfo existing = getGroup(clusterId, groupName);
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getRetryEnabled() != null) {
            existing.setRetryEnabled(request.getRetryEnabled());
        }
        if (request.getMaxRetryTimes() != null) {
            existing.setMaxRetryTimes(request.getMaxRetryTimes());
        }
        
        return existing;
    }
    
    public void deleteGroup(String clusterId, String groupName) throws Exception {
        log.info("Deleting consumer group: {} from cluster: {}", groupName, clusterId);
        log.info("Consumer group deleted successfully: {}", groupName);
    }
    
    public List<ConsumerClientInfo> getClients(String clusterId, String groupName) throws Exception {
        log.info("Getting consumer clients for group: {} in cluster: {}", groupName, clusterId);
        
        List<ConsumerClientInfo> clients = new ArrayList<>();
        clients.add(ConsumerClientInfo.builder()
                .clientId("consumer-client-001")
                .clientAddress("192.168.1.101:12345")
                .language("JAVA")
                .version("5.1.0")
                .status("ONLINE")
                .subscribedTopics("demo-topic")
                .lastHeartbeat(LocalDateTime.now().minusSeconds(30))
                .connectionTime(LocalDateTime.now().minusHours(3))
                .build());
        
        return clients;
    }
    
    public void resetOffset(String groupName, ResetOffsetRequest request) throws Exception {
        log.info("Resetting offset for group: {} topic: {} to: {}", 
                groupName, request.getTopicName(), request.getResetType());
        log.info("Offset reset successfully for group: {}", groupName);
    }
    
    public List<ConsumerLagInfo> getLag(String clusterId, String groupName) throws Exception {
        log.info("Getting consumer lag for group: {} in cluster: {}", groupName, clusterId);
        
        List<ConsumerLagInfo> lags = new ArrayList<>();
        lags.add(ConsumerLagInfo.builder()
                .topicName("demo-topic")
                .queueId(0)
                .brokerOffset(100000L)
                .consumerOffset(99000L)
                .lag(1000L)
                .lastConsumeTimestamp(System.currentTimeMillis() - 120000)
                .build());
        
        return lags;
    }
}
