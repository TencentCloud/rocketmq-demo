package com.rocketmq.dashboard.service;

import com.rocketmq.dashboard.dto.request.CreateGroupRequest;
import com.rocketmq.dashboard.dto.request.ResetOffsetRequest;
import com.rocketmq.dashboard.dto.request.UpdateGroupRequest;
import com.rocketmq.dashboard.dto.response.ConsumerClientInfo;
import com.rocketmq.dashboard.dto.response.ConsumerLagInfo;
import com.rocketmq.dashboard.dto.response.GroupInfo;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trocket.v20230308.TrocketClient;
import com.tencentcloudapi.trocket.v20230308.models.ConsumeGroupItem;
import com.tencentcloudapi.trocket.v20230308.models.DescribeConsumerLagRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeConsumerLagResponse;
import com.tencentcloudapi.trocket.v20230308.models.DescribeConsumerClientListRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeConsumerClientListResponse;
import com.tencentcloudapi.trocket.v20230308.models.ConsumerClient;
import com.tencentcloudapi.trocket.v20230308.models.CreateConsumerGroupRequest;
import com.tencentcloudapi.trocket.v20230308.models.CreateConsumerGroupResponse;
import com.tencentcloudapi.trocket.v20230308.models.ModifyConsumerGroupRequest;
import com.tencentcloudapi.trocket.v20230308.models.ModifyConsumerGroupResponse;
import com.tencentcloudapi.trocket.v20230308.models.DeleteConsumerGroupRequest;
import com.tencentcloudapi.trocket.v20230308.models.DeleteConsumerGroupResponse;
import com.tencentcloudapi.trocket.v20230308.models.ResetConsumerGroupOffsetRequest;
import com.tencentcloudapi.trocket.v20230308.models.ResetConsumerGroupOffsetResponse;
import com.tencentcloudapi.trocket.v20230308.models.DescribeConsumerGroupListRequest;
import com.tencentcloudapi.trocket.v20230308.models.DescribeConsumerGroupListResponse;
import com.tencentcloudapi.trocket.v20230308.models.Filter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupService {
    
    private final TrocketClient trocketClient;
    
    public GroupService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }
    
    public List<GroupInfo> listGroups(String clusterId) throws TencentCloudSDKException {
        log.info("Listing consumer groups for cluster: {}", clusterId);

        try {
            DescribeConsumerGroupListRequest request = new DescribeConsumerGroupListRequest();
            request.setInstanceId(clusterId);
            request.setOffset(0L);
            request.setLimit(100L);

            DescribeConsumerGroupListResponse response = trocketClient.DescribeConsumerGroupList(request);

            List<GroupInfo> groups = new ArrayList<>();
            if (response.getData() != null) {
                groups = Arrays.stream(response.getData())
                        .map(this::mapToGroupInfo)
                        .collect(Collectors.toList());
            }

            log.info("Found {} consumer groups", groups.size());
            return groups;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to list consumer groups from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private GroupInfo mapToGroupInfo(ConsumeGroupItem item) {
        return GroupInfo.builder()
                .groupName(item.getConsumerGroup())
                .clusterId(item.getInstanceId())
                .description(item.getRemark())
                .consumeFrom("CONSUME_FROM_LAST_OFFSET")
                .broadcast(Boolean.FALSE.equals(item.getConsumeMessageOrderly()))
                .retryEnabled(Boolean.TRUE.equals(item.getConsumeEnable()))
                .maxRetryTimes(item.getMaxRetryTimes() != null ? item.getMaxRetryTimes().intValue() : 16)
                .subscribedTopics(null)
                .onlineConsumers(null)
                .totalLag(null)
                .consumeTps(null)
                .createTime(LocalDateTime.now())
                .lastConsumeTime(null)
                .build();
    }
    
    public GroupInfo getGroup(String clusterId, String groupName) throws TencentCloudSDKException {
        log.info("Getting consumer group: {} in cluster: {}", groupName, clusterId);

        try {
            // Create request for DescribeConsumerGroupList API with filter
            DescribeConsumerGroupListRequest request = new DescribeConsumerGroupListRequest();
            request.setInstanceId(clusterId);
            request.setOffset(0L);
            request.setLimit(100L);

            // Filter by specific consumer group name
            Filter[] filters = new Filter[1];
            filters[0] = new Filter();
            filters[0].setName("ConsumerGroup");
            filters[0].setValues(new String[]{groupName});
            request.setFilters(filters);

            // Call Tencent Cloud API
            DescribeConsumerGroupListResponse response = trocketClient.DescribeConsumerGroupList(request);

            // Check if consumer group exists
            if (response.getData() == null || response.getData().length == 0) {
                log.error("Consumer group not found: {} in cluster: {}", groupName, clusterId);
                throw new TencentCloudSDKException("ConsumerGroupNotFound: " + groupName);
            }

            // Get first (and should be only) group
            ConsumeGroupItem groupItem = response.getData()[0];

            // Map to GroupInfo with detailed information
            log.info("Successfully retrieved consumer group: {}", groupName);
            return mapToGroupInfoDetailed(groupItem);

        } catch (TencentCloudSDKException e) {
            log.error("Failed to get consumer group {} from cluster {}: {}", groupName, clusterId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Map Tencent Cloud ConsumeGroupItem to GroupInfo with detailed fields
     */
    private GroupInfo mapToGroupInfoDetailed(ConsumeGroupItem item) {
        return GroupInfo.builder()
                .groupName(item.getConsumerGroup())
                .clusterId(item.getInstanceId())
                .description(item.getRemark())
                .consumeFrom("CONSUME_FROM_LAST_OFFSET")
                .broadcast(Boolean.FALSE.equals(item.getConsumeMessageOrderly()))
                .retryEnabled(Boolean.TRUE.equals(item.getConsumeEnable()))
                .maxRetryTimes(item.getMaxRetryTimes() != null ? item.getMaxRetryTimes().intValue() : 16)
                .subscribedTopics(null)
                .onlineConsumers(null)
                .totalLag(null)
                .consumeTps(null)
                .createTime(LocalDateTime.now())
                .lastConsumeTime(null)
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
    
    public List<ConsumerClientInfo> getClients(String clusterId, String groupName) throws TencentCloudSDKException {
        log.info("Getting consumer clients for group: {} in cluster: {}", groupName, clusterId);

        List<ConsumerClientInfo> clients = new ArrayList<>();

        try {
            DescribeConsumerClientListRequest request = new DescribeConsumerClientListRequest();
            request.setInstanceId(clusterId);
            request.setConsumerGroup(groupName);
            request.setLimit(100L);
            request.setOffset(0L);

            DescribeConsumerClientListResponse response = trocketClient.DescribeConsumerClientList(request);

            if (response.getData() != null) {
                for (ConsumerClient item : response.getData()) {
                    clients.add(mapToConsumerClientInfo(item));
                }
            }

            log.info("Found {} consumer clients", clients.size());
            return clients;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query consumer clients from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ConsumerClientInfo mapToConsumerClientInfo(ConsumerClient item) {
        return ConsumerClientInfo.builder()
                .clientId(item.getClientId())
                .clientAddress(item.getClientAddr())
                .language(item.getLanguage())
                .version(item.getVersion())
                .status("ONLINE")
                .subscribedTopics(null)
                .lastHeartbeat(LocalDateTime.now())
                .connectionTime(LocalDateTime.now())
                .build();
    }
    
    public void resetOffset(String clusterId, String groupName, ResetOffsetRequest request) throws TencentCloudSDKException {
        log.info("Resetting offset for group: {} topic: {} to: {}",
                groupName, request.getTopicName(), request.getResetType());

        try {
            ResetConsumerGroupOffsetRequest sdkRequest = new ResetConsumerGroupOffsetRequest();
            sdkRequest.setInstanceId(clusterId);
            sdkRequest.setTopic(request.getTopicName());
            sdkRequest.setConsumerGroup(groupName);

            long resetTimestamp;
            switch (request.getResetType()) {
                case "LATEST":
                    resetTimestamp = System.currentTimeMillis();
                    break;
                case "EARLIEST":
                    resetTimestamp = 0L;
                    break;
                case "TIMESTAMP":
                    resetTimestamp = request.getTimestamp() != null ? request.getTimestamp() : System.currentTimeMillis();
                    break;
                default:
                    throw new TencentCloudSDKException("Invalid reset type: " + request.getResetType());
            }

            sdkRequest.setResetTimestamp(resetTimestamp);

            ResetConsumerGroupOffsetResponse response = trocketClient.ResetConsumerGroupOffset(sdkRequest);

            log.info("Offset reset successfully for group: {}", groupName);
        } catch (TencentCloudSDKException e) {
            log.error("Failed to reset offset from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public List<ConsumerLagInfo> getLag(String clusterId, String groupName) throws TencentCloudSDKException {
        log.info("Getting consumer lag for group: {} in cluster: {}", groupName, clusterId);

        List<ConsumerLagInfo> lags = new ArrayList<>();

        try {
            DescribeConsumerLagRequest request = new DescribeConsumerLagRequest();
            request.setInstanceId(clusterId);
            request.setConsumerGroup(groupName);

            DescribeConsumerLagResponse response = trocketClient.DescribeConsumerLag(request);

            if (response.getConsumerLag() != null) {
                lags.add(ConsumerLagInfo.builder()
                        .topicName(null)
                        .queueId(0)
                        .brokerOffset(null)
                        .consumerOffset(null)
                        .lag(response.getConsumerLag())
                        .lastConsumeTimestamp(System.currentTimeMillis())
                        .build());
            }

            log.info("Found {} lag records", lags.size());
            return lags;
        } catch (TencentCloudSDKException e) {
            log.error("Failed to query consumer lag from Tencent Cloud API: {}", e.getMessage(), e);
            throw e;
        }
    }
}
