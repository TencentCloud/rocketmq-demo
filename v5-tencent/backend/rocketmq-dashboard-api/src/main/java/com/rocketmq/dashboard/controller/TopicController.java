package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.request.CreateTopicRequest;
import com.rocketmq.dashboard.dto.request.UpdateTopicRequest;
import com.rocketmq.dashboard.dto.response.ProducerInfo;
import com.rocketmq.dashboard.dto.response.TopicInfo;
import com.rocketmq.dashboard.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/topics")
@Tag(name = "Topic Management", description = "APIs for managing RocketMQ topics")
public class TopicController {
    
    private final TopicService topicService;
    
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }
    
    @GetMapping
    @Operation(summary = "Get topic list", description = "Retrieve all topics in a cluster")
    public Result<List<TopicInfo>> listTopics(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Listing topics for cluster: {}", clusterId);
            List<TopicInfo> topics = topicService.listTopics(clusterId);
            return Result.success(topics);
        } catch (Exception e) {
            log.error("Failed to list topics for cluster: {}", clusterId, e);
            throw new RuntimeException("Failed to list topics: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{name}")
    @Operation(summary = "Get topic details", description = "Retrieve detailed information about a specific topic")
    public Result<TopicInfo> getTopic(
            @Parameter(description = "Topic name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Getting topic: {} in cluster: {}", name, clusterId);
            TopicInfo topic = topicService.getTopic(clusterId, name);
            return Result.success(topic);
        } catch (Exception e) {
            log.error("Failed to get topic: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to get topic: " + e.getMessage(), e);
        }
    }
    
    @PostMapping
    @Operation(summary = "Create topic", description = "Create a new topic in a cluster")
    public Result<TopicInfo> createTopic(@Valid @RequestBody CreateTopicRequest request) {
        try {
            log.info("Creating topic: {} in cluster: {}", request.getTopicName(), request.getClusterId());
            TopicInfo topic = topicService.createTopic(request);
            return Result.success("Topic created successfully", topic);
        } catch (Exception e) {
            log.error("Failed to create topic: {}", request.getTopicName(), e);
            throw new RuntimeException("Failed to create topic: " + e.getMessage(), e);
        }
    }
    
    @PutMapping("/{name}")
    @Operation(summary = "Update topic", description = "Update topic configuration")
    public Result<TopicInfo> updateTopic(
            @Parameter(description = "Topic name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Valid @RequestBody UpdateTopicRequest request) {
        try {
            log.info("Updating topic: {} in cluster: {}", name, clusterId);
            TopicInfo topic = topicService.updateTopic(clusterId, name, request);
            return Result.success("Topic updated successfully", topic);
        } catch (Exception e) {
            log.error("Failed to update topic: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to update topic: " + e.getMessage(), e);
        }
    }
    
    @DeleteMapping("/{name}")
    @Operation(summary = "Delete topic", description = "Delete a topic from a cluster")
    public Result<String> deleteTopic(
            @Parameter(description = "Topic name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Deleting topic: {} from cluster: {}", name, clusterId);
            topicService.deleteTopic(clusterId, name);
            return Result.success("Topic deleted successfully", "OK");
        } catch (Exception e) {
            log.error("Failed to delete topic: {} from cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to delete topic: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{name}/producers")
    @Operation(summary = "Get topic producers", description = "Retrieve list of producers for a specific topic")
    public Result<List<ProducerInfo>> listProducers(
            @Parameter(description = "Topic name", required = true)
            @PathVariable String name,
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId) {
        try {
            log.info("Listing producers for topic: {} in cluster: {}", name, clusterId);
            List<ProducerInfo> producers = topicService.getProducers(clusterId, name);
            return Result.success(producers);
        } catch (Exception e) {
            log.error("Failed to list producers for topic: {} in cluster: {}", name, clusterId, e);
            throw new RuntimeException("Failed to list producers: " + e.getMessage(), e);
        }
    }
}
