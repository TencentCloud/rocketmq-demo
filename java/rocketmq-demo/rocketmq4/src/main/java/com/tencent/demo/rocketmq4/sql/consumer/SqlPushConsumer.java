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
package com.tencent.demo.rocketmq4.sql.consumer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;

/**
 * Description: 消息消费者(push)
 */
public class SqlPushConsumer {


    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic";

    /**
     * 消费者组名称
     */
    private static final String GROUP_NAME = "group";

    public static void main(String[] args) throws Exception {
        // 创建消息消费者
        DefaultMQPushConsumer pushConsumer = ClientCreater.createPushConsumer(GROUP_NAME);

        //订阅所有消息
        pushConsumer.subscribe(TOPIC_NAME, MessageSelector.bySql("True"));

        // 订阅topic 订阅单个key的sql
        pushConsumer.subscribe(TOPIC_NAME,
            MessageSelector.bySql("key1 IS NOT NULL AND key1='value1'"));

        //订阅多个属性
        pushConsumer.subscribe(TOPIC_NAME,
            MessageSelector.bySql("key1 IS NOT NULL AND key2 IS NOT NULL  AND key1='value1' AND key2='value2'"));

        // 注册回调实现类来处理从broker拉取回来的消息
        pushConsumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            // 消息处理逻辑
            System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
            // 标记该消息已经被成功消费
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        // 启动消费者实例
        System.out.printf("Consumer Started.%n");
        pushConsumer.start();
        System.in.read();
        pushConsumer.shutdown();
    }
}