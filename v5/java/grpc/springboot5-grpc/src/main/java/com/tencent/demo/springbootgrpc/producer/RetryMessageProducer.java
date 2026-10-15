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

import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description: 向重试示例 Topic 发送普通消息，由 RetryMessageConsumer 模拟消费失败触发重试
 */
@Service
public class RetryMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(RetryMessageProducer.class);

    @Value("${demo.rocketmq.retry-topic}")
    private String topic;

    @Autowired
    private RocketMQClientTemplate rocketMQClientTemplate;

    public void send(String payload) {
        SendReceipt sendReceipt = rocketMQClientTemplate.syncSendNormalMessage(topic, payload);
        log.info("Send message successfully, topic={}, messageId={}", topic, sendReceipt.getMessageId());
    }
}
