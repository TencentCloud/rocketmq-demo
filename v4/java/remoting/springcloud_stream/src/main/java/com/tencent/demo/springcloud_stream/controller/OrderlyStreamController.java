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

import java.util.ArrayList;
import java.util.List;

import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 顺序消息 - 生产者
 * 顺序消息需要使用顺序类型的topic（支持全局顺序和局部顺序两种顺序类型）；
 * orderlyProducer-out-0 在 application.yml 中配置了 message-queue-selector: orderMessageQueueSelector，
 * 同一订单号（消息 key）的消息总是发到同一个队列。
 */
@RestController
public class OrderlyStreamController {

    private final Logger logger = LoggerFactory.getLogger(OrderlyStreamController.class);

    private final StreamBridge streamBridge;

    public OrderlyStreamController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * 测试顺序消息（按订单号分区有序）
     */
    @GetMapping("test-orderly")
    public String testOrderlyStream() {
        String[] steps = {"创建", "支付", "发货", "完成"};
        List<String> failed = new ArrayList<>();
        for (String step : steps) {
            for (int i = 0; i < 3; i++) {
                String orderId = "order-" + i;
                Message<String> message = MessageBuilder.withPayload(orderId + " " + step)
                        .setHeader(MessageConst.PROPERTY_KEYS, orderId) // 业务key（订单号），队列选择器按它选择队列
                        .build();
                boolean sent = streamBridge.send("orderlyProducer-out-0", message);
                logger.info("Send: 发送顺序消息，sent = {}, messageBody = {}", sent, message.getPayload());
                if (!sent) {
                    failed.add(message.getPayload());
                }
            }
        }
        return failed.isEmpty() ? "success" : "fail: " + failed;
    }
}
