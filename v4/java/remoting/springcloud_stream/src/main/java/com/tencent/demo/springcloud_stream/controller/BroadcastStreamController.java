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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 广播消息 - 生产者
 * 生产端与普通消息相同（Topic 类型：普通），广播效果由消费端 message-model: BROADCASTING 决定。
 */
@RestController
public class BroadcastStreamController {

    private final Logger logger = LoggerFactory.getLogger(BroadcastStreamController.class);

    private final StreamBridge streamBridge;

    public BroadcastStreamController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * 测试广播消息
     * 启动多个实例（如 --server.port=8081）后调用，同一消费组的每个实例都会收到全部消息
     */
    @GetMapping("test-broadcast")
    public String testBroadcastStream() {
        int failed = 0;
        for (int i = 0; i < 5; i++) {
            Message<String> message = MessageBuilder.withPayload("this is new broadcast message " + i).build();
            boolean sent = streamBridge.send("broadcastProducer-out-0", message);
            logger.info("Send: 发送广播消息，sent = {}, messageBody = {}", sent, message.getPayload());
            if (!sent) {
                failed++;
            }
        }
        return failed == 0 ? "success" : "fail: " + failed;
    }
}
