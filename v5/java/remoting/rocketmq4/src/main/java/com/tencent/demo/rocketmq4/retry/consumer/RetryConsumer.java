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
package com.tencent.demo.rocketmq4.retry.consumer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;

/**
 * Description: 重试消费
 */
public class RetryConsumer {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic3";

    /**
     * 消费者组名称
     */
    private static final String GROUP_NAME = "group3";

    public static void main(String[] args) throws Exception {
        // 创建消息消费者
        DefaultMQPushConsumer pushConsumer = ClientCreater.createPushConsumer(GROUP_NAME);
        // 订阅topic
        pushConsumer.subscribe(TOPIC_NAME, "*");
        // 设置重试次数, 重试结束仍未消费成功，进入死信队列
        pushConsumer.setMaxReconsumeTimes(3);
        // 注册回调实现类来处理从broker拉取回来的消息
        pushConsumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            // 消息处理逻辑
            System.out.printf("%d %s Receive New Messages: %s %n", System.currentTimeMillis(),
                    Thread.currentThread().getName(), msgs);
            // 标记该消息消费失败 (模拟消费失败)
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        });
        // 启动消费者实例
        pushConsumer.start();
        System.out.println("Consumer Started.");
    }

}
