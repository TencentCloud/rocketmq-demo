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
package com.tencent.demo.springboot.consumer;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Description: 重试消息消费者
 *
 * <p>集群模式（默认 CLUSTERING）下，onMessage 抛出异常时 starter 会捕获异常并返回 RECONSUME_LATER，
 * 消息稍后会被重新投递，每次重新投递 reconsumeTimes 加 1。
 * <p>maxReconsumeTimes 为最大重试次数，不设置（-1）时并发消费默认 16 次；超过最大重试次数后消息进入死信队列 %DLQ%消费组名称，
 * 需要在腾讯云控制台查看/处理死信消息。
 * <p>广播模式（BROADCASTING）下消费失败不会重试。
 * <p>注意：生产者的 retry-times-when-send-failed 是发送失败重试，不是消费重试。
 * <p>腾讯云上需要先创建 Topic（普通消息类型）和消费组，名称与 application.yml 中 rocketmq.retry 配置一致。
 */
@Service
@RocketMQMessageListener(
    consumerGroup = "${rocketmq.namespace}%${rocketmq.retry.group}",  // 消费组，格式：namespace全称%group名称
    // 需要使用topic全称，所以进行topic名称的拼接，也可以自己设置  格式：namespace全称%topic名称
    topic = "${rocketmq.namespace}%${rocketmq.retry.topic}",
    selectorExpression = "*", // 订阅表达式, * 表示订阅所有消息
    enableMsgTrace = true, //打开轨迹
    maxReconsumeTimes = 3 // 最大重试次数，超过后进入死信队列；不设置时默认 16 次
)
public class RetryConsumer implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        System.out.printf("RetryConsumer receive message: msgId=%s, reconsumeTimes=%d, body=%s %n",
            message.getMsgId(), message.getReconsumeTimes(), body);
        // 模拟前 2 次消费失败：抛出异常触发重试，第 3 次投递（reconsumeTimes=2）时消费成功
        // 如果一直抛异常，重试 maxReconsumeTimes 次后消息会进入死信队列
        if (message.getReconsumeTimes() < 2) {
            throw new RuntimeException("mock consume failed, msgId=" + message.getMsgId()
                + ", reconsumeTimes=" + message.getReconsumeTimes());
        }
        System.out.printf("RetryConsumer consume success: msgId=%s, reconsumeTimes=%d %n",
            message.getMsgId(), message.getReconsumeTimes());
    }
}
