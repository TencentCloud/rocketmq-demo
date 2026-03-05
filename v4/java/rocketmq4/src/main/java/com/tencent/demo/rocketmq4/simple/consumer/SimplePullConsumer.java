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
package com.tencent.demo.rocketmq4.simple.consumer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * Description: 消息消费者(pull)
 */
public class SimplePullConsumer {

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
        DefaultLitePullConsumer pullConsumer = ClientCreater.createPullConsumer(GROUP_NAME);
        // 订阅topic
        pullConsumer.subscribe(TOPIC_NAME, "*");

        pullConsumer.start();
        try {
            System.out.printf("Consumer Started.%n");
            while (true) {
                // 拉取消息
                List<MessageExt> messageExts = pullConsumer.poll();
                System.out.printf("%s%n", messageExts.size());
                if (!messageExts.isEmpty()) {
                    System.out.println("拉取到消息: " + messageExts.get(0));
                    pullConsumer.commitSync();
                }
            }
        } finally {
            pullConsumer.shutdown();
        }
    }
}