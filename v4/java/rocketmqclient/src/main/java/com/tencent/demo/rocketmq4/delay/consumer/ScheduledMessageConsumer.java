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
package com.tencent.demo.rocketmq4.delay.consumer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 延迟消息消费者
 */
public class ScheduledMessageConsumer {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic2";

    /**
     * 消费者组名称
     */
    private static final String GROUP_NAME = "group2x";

     public static void main(String[] args) throws Exception {
         // 创建消息消费者
         DefaultMQPushConsumer consumer = ClientCreater.createPushConsumer(GROUP_NAME);

         // 订阅topic
         consumer.subscribe(TOPIC_NAME, "*");
         // 注册回调实现类来处理从broker拉取回来的消息
         consumer.registerMessageListener(new MessageListenerConcurrently() {
             @Override
             public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                 for (MessageExt message : messages) {
                     System.out.println("Receive message = " + message);
                 }
                 // 回复消费成功
                 return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
             }
         });
         // 启动消费者
         consumer.start();
         System.out.println("Consumer Started.");
     }
 }
