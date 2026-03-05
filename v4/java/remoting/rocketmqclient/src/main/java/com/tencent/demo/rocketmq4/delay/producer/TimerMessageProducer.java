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
package com.tencent.demo.rocketmq4.delay.producer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * 任意延迟的定时消息
 */
public class TimerMessageProducer {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic2";

    /**
     * 生产者组名称
     */
    private static final String GROUP_NAME = "group2";

    public static void main(String[] args) throws Exception {
        // 创建消息消费者
        DefaultMQProducer producer = ClientCreater.createProducer(GROUP_NAME);

        int totalMessagesToSend = 1;
        for (int i = 0; i < totalMessagesToSend; i++) {
            Message message = new Message(TOPIC_NAME, ("Hello timer message " + i).getBytes());
            // 设置发送消息的时间
            long timeStamp = System.currentTimeMillis() + 30000;
            // 若需要发送定时消息，则需要设置定时时间，消息将在指定时间进行投递，例如消息将在2022-08-08 08:08:08投递。
            // 若设置的时间戳在当前时间之前，则消息将被立即投递给Consumer。
            //将 __STARTDELIVERTIME 设定到 msg 的属性中
            message.putUserProperty("__STARTDELIVERTIME", String.valueOf(timeStamp));
            // 发送消息
            SendResult sendResult = producer.send(message);
            System.out.println("sendResult = " + sendResult);
        }

        producer.shutdown();
    }
}
