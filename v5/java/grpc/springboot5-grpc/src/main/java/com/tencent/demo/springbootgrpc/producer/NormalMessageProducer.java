/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.tencent.demo.springbootgrpc.producer;

import java.util.concurrent.CompletableFuture;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Description: 普通消息生产者（同步/异步发送），Topic 类型需为「普通」
 */
@Service
public class NormalMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(NormalMessageProducer.class);

    @Value("${demo.rocketmq.normal-topic}")
    private String topic;

    @Autowired
    private RocketMQClientTemplate rocketMQClientTemplate;

    /**
     * 同步发送，发送失败会抛出 MessagingException
     */
    public void syncSend(String payload) {
        Message<String> message = MessageBuilder.withPayload(payload)
            // 消息的业务 key，可在控制台按 key 查询消息
            .setHeader(RocketMQHeaders.KEYS, "yourMessageKey")
            .build();
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(topic, message);
        log.info("Send message successfully, topic={}, messageId={}", topic, sendReceipt.getMessageId());
    }

    /**
     * 异步发送，发送结果在回调中处理
     */
    public void asyncSend(String payload) {
        CompletableFuture<SendReceipt> future = new CompletableFuture<>();
        future.whenComplete((sendReceipt, throwable) -> {
            if (throwable != null) {
                log.error("Failed to send message asynchronously, topic={}", topic, throwable);
                return;
            }
            log.info("Send message asynchronously successfully, topic={}, messageId={}", topic,
                sendReceipt.getMessageId());
        });
        rocketMQClientTemplate.asyncSendNormalMessage(topic, payload, future);
    }
}
