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
package com.tencent.demo.springcloud_stream.controller;

import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 重试消息 - 生产者
 * 发送一条普通消息到 retryProducer-out-0（Topic 类型：普通），由 retryConsumer 消费失败来演示服务端重试。
 */
@RestController
public class RetryStreamController {

    private final Logger logger = LoggerFactory.getLogger(RetryStreamController.class);

    private final StreamBridge streamBridge;

    public RetryStreamController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * 测试重试消息
     * 注意：生产者的 retry-times-when-send-failed 是"发送失败"重试，与消费重试无关
     */
    @GetMapping("test-retry")
    public String testRetryStream() {
        Message<String> message = MessageBuilder.withPayload("this is new retry message.")
                .setHeader(MessageConst.PROPERTY_KEYS, "retryKey") // 设置消息业务key，便于在控制台按key查询重试/死信消息
                .build();
        boolean sent = streamBridge.send("retryProducer-out-0", message);
        logger.info("Send: 发送重试演示消息，sent = {}, messageBody = {}", sent, message.getPayload());
        return sent ? "success" : "fail";
    }
}
