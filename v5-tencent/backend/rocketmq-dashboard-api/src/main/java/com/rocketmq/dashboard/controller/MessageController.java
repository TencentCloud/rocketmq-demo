package com.rocketmq.dashboard.controller;

import com.rocketmq.dashboard.common.Result;
import com.rocketmq.dashboard.dto.request.QueryMessagesRequest;
import com.rocketmq.dashboard.dto.request.SendMessageRequest;
import com.rocketmq.dashboard.dto.request.VerifyMessageRequest;
import com.rocketmq.dashboard.dto.response.MessageInfo;
import com.rocketmq.dashboard.dto.response.MessageTraceInfo;
import com.rocketmq.dashboard.dto.response.SendMessageResponse;
import com.rocketmq.dashboard.dto.response.VerifyMessageResponse;
import com.rocketmq.dashboard.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@Tag(name = "Message Management", description = "APIs for managing RocketMQ messages")
public class MessageController {
    
    private final MessageService messageService;
    
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    @GetMapping
    @Operation(summary = "Query message list", description = "Query messages by topic, key, messageId or time range")
    public Result<List<MessageInfo>> queryMessages(@Valid QueryMessagesRequest request) {
        try {
            log.info("Querying messages with request: {}", request);
            List<MessageInfo> messages = messageService.queryMessages(request);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("Failed to query messages", e);
            throw new RuntimeException("Failed to query messages: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Query message details", description = "Retrieve detailed information about a specific message")
    public Result<MessageInfo> getMessageById(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Message ID", required = true, example = "01000000000000000000000000000000")
            @PathVariable String id) {
        try {
            log.info("Getting message details for id: {}", id);
            MessageInfo message = messageService.getMessageById(clusterId, id);
            return Result.success(message);
        } catch (Exception e) {
            log.error("Failed to get message: {}", id, e);
            throw new RuntimeException("Failed to get message: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{id}/trace")
    @Operation(summary = "Query message trace", description = "Query the complete trace information of a message")
    public Result<List<MessageTraceInfo>> getMessageTrace(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Message ID", required = true, example = "01000000000000000000000000000000")
            @PathVariable String id) {
        try {
            log.info("Getting message trace for id: {}", id);
            List<MessageTraceInfo> traces = messageService.getMessageTrace(clusterId, id);
            return Result.success(traces);
        } catch (Exception e) {
            log.error("Failed to get message trace: {}", id, e);
            throw new RuntimeException("Failed to get message trace: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/send")
    @Operation(summary = "Send message", description = "Send a message to a specified topic")
    public Result<SendMessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            log.info("Sending message to topic: {}", request.getTopicName());
            SendMessageResponse response = messageService.sendMessage(request);
            return Result.success("Message sent successfully", response);
        } catch (Exception e) {
            log.error("Failed to send message", e);
            throw new RuntimeException("Failed to send message: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/{id}/resend")
    @Operation(summary = "Resend dead letter message", description = "Resend a message from dead letter queue")
    public Result<SendMessageResponse> resendDeadLetterMessage(
            @Parameter(description = "Cluster ID", required = true, example = "rmq-cn-xxxxx")
            @RequestParam String clusterId,
            @Parameter(description = "Message ID", required = true, example = "01000000000000000000000000000000")
            @PathVariable String id) {
        try {
            log.info("Resending dead letter message: {}", id);
            SendMessageResponse response = messageService.resendDeadLetterMessage(clusterId, id);
            return Result.success("Dead letter message resent successfully", response);
        } catch (Exception e) {
            log.error("Failed to resend dead letter message: {}", id, e);
            throw new RuntimeException("Failed to resend dead letter message: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/verify")
    @Operation(summary = "Verify message consumption", description = "Verify if a message has been consumed by a consumer group")
    public Result<VerifyMessageResponse> verifyMessageConsumption(@Valid @RequestBody VerifyMessageRequest request) {
        try {
            log.info("Verifying message consumption for messageId: {}, group: {}", 
                    request.getMessageId(), request.getConsumerGroup());
            VerifyMessageResponse response = messageService.verifyMessageConsumption(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to verify message consumption", e);
            throw new RuntimeException("Failed to verify message consumption: " + e.getMessage(), e);
        }
    }
}
