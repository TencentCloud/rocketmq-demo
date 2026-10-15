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
package com.tencent.demo.springbootgrpc.consumer;

import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Description: 事务消息消费者，只能消费到已 commit 的事务消息
 */
@Service
@RocketMQMessageListener(
    // endpoints/accessKey/secretKey 不配置时默认读取 rocketmq.push-consumer.* 配置
    // 控制台创建的 Topic 名称
    topic = "${demo.rocketmq.transaction-topic}",
    // 控制台创建的消费组
    consumerGroup = "${demo.rocketmq.transaction-consumer-group}",
    // 订阅全部 tag
    tag = "*",
    // starter 默认开启 SSL；与 Java 原生示例一致，访问腾讯云 8080 接入点时关闭
    sslEnabled = false
)
public class TransactionMessageConsumer implements RocketMQListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionMessageConsumer.class);

    @Override
    public ConsumeResult consume(MessageView messageView) {
        log.info("Consume transaction message, messageId={}, properties={}, body={}", messageView.getMessageId(),
            messageView.getProperties(), StandardCharsets.UTF_8.decode(messageView.getBody()).toString());
        return ConsumeResult.SUCCESS;
    }
}
