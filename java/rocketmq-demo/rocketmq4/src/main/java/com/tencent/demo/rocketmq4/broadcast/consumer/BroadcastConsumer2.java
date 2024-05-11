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
package com.tencent.demo.rocketmq4.broadcast.consumer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * 消费者2(广播类型)
 */
public class BroadcastConsumer2 {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic1";

    /**
     * 消费者组名称
     */
    private static final String GROUP_NAME = "group11";

    public static void main(String[] args) throws Exception {
        // 创建消息消费者
        DefaultMQPushConsumer pushConsumer = ClientCreater.createPushConsumer(GROUP_NAME);
        // 设置从第一个偏移量开始消费
        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 设置消息模型为广播模型
        pushConsumer.setMessageModel(MessageModel.BROADCASTING);
        // 订阅消息并设置tag
        pushConsumer.subscribe(TOPIC_NAME, "*");
        // 注册回调实现类来处理从broker拉取回来的消息
        pushConsumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动消费者
        pushConsumer.start();
        System.out.printf("Broadcast Consumer2 Started.%n");
    }
}
