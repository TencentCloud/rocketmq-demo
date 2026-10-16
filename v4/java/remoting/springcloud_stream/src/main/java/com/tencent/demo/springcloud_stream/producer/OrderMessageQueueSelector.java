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
package com.tencent.demo.springcloud_stream.producer;

import java.util.List;

import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.stereotype.Component;

/**
 * 顺序消息 - 队列选择器
 * Bean 名称 orderMessageQueueSelector 需要与 application.yml 中
 * spring.cloud.stream.rocketmq.bindings.orderlyProducer-out-0.producer.message-queue-selector 一致。
 * 按消息 key（订单号）哈希选择队列，保证同一订单的消息进入同一队列。
 */
@Component("orderMessageQueueSelector")
public class OrderMessageQueueSelector implements MessageQueueSelector {

    /**
     * @param mqs topic 的全部可写队列
     * @param msg 待发送的 RocketMQ 消息（KEYS 消息头已转换为消息 key）
     * @param arg binder 传入的 Spring MessageHeaders，这里不使用
     * @return 选中的队列
     */
    @Override
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        String orderId = msg.getKeys();
        if (orderId == null || orderId.isEmpty()) {
            throw new IllegalArgumentException("顺序消息必须设置 KEYS 消息头（订单号）");
        }
        return mqs.get(Math.floorMod(orderId.hashCode(), mqs.size()));
    }
}
