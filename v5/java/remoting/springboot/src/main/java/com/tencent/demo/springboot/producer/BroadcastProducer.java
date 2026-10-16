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
package com.tencent.demo.springboot.producer;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Description: 广播消息示例的生产者，向 BroadcastConsumer 订阅的 Topic 发送普通消息
 *
 * <p>广播与否由消费端的 messageModel 决定，生产端与普通消息发送方式相同。
 */
@Service
public class BroadcastProducer {

    // 需要使用topic全称，所以进行topic名称的拼接，也可以自己设置  格式：namespace全称%topic名称
    @Value("${rocketmq.namespace}%${rocketmq.broadcast.topic}")
    private String topic;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送
     *
     * @param message 消息内容
     * @param key     业务key
     */
    public void syncSend(String message, String key) {
        SendResult sendResult = rocketMQTemplate.syncSend(topic,
                MessageBuilder.withPayload(message)
                        .setHeader(RocketMQHeaders.KEYS, key)   // 指定业务key
                        .build());
        System.out.printf("broadcast syncSend to topic %s sendResult=%s %n", topic, sendResult);
    }
}
