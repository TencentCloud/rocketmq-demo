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

import java.time.Duration;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description: 定时/延迟消息生产者，Topic 类型需为「定时/延时」
 */
@Service
public class DelayMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(DelayMessageProducer.class);

    @Value("${demo.rocketmq.delay-topic}")
    private String topic;

    @Autowired
    private RocketMQClientTemplate rocketMQClientTemplate;

    /**
     * starter 会把延迟时长转换为 deliveryTimestamp = 当前时间 + delay。
     * 如需在指定时刻投递（定时消息），可传入 Duration.between(Instant.now(), 目标时刻)。
     */
    public void send(String payload, Duration delay) {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendDelayMessage(topic, payload, delay);
        log.info("Send delay message successfully, topic={}, delay={}, messageId={}", topic, delay,
            sendReceipt.getMessageId());
    }
}
