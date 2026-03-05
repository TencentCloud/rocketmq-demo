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
package com.tencent.demo.rocketmq4.simple.producer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Description: 异步发送消息
 */
public class AsyncSendMessage {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic";

    /**
     * 生产者组名称
     */
    private static final String GROUP_NAME = "group";


    public static void main(String[] args) throws Exception {
        // 创建消息生产者
        DefaultMQProducer producer = ClientCreater.createProducer(GROUP_NAME);
        // 设置发送失败后不重试
        producer.setRetryTimesWhenSendAsyncFailed(0);
        // 设置发送消息的数量
        int messageCount = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(messageCount);
        for (int i = 0; i < messageCount; i++) {
            try {
                final int index = i;
                // 创建消息实体，设置topic和消息内容
                Message msg = new Message(TOPIC_NAME, "TAG", ("Hello rocketMq " + index).getBytes(StandardCharsets.UTF_8));
                producer.send(msg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        // 消息发送成功逻辑
                        countDownLatch.countDown();
                        System.out.printf("%-10d OK %s %n", index, sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        // 消息发送失败逻辑
                        countDownLatch.countDown();
                        System.out.printf("%-10d Exception %s %n", index, e);
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        countDownLatch.await(5, TimeUnit.SECONDS);
        producer.shutdown();
    }
}