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
package com.tencent.demo.rocketmq4.retry.producer;

import com.tencent.demo.rocketmq4.common.ClientCreater;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * Description: 同步发送消息
 */
public class RetryProducer {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic3";

    /**
     * 生产者组名称
     */
    private static final String GROUP_NAME = "group3";


    public static void main(String[] args) throws Exception {
        // 创建消息生产者
        DefaultMQProducer producer = ClientCreater.createProducer(GROUP_NAME);
        for (int i = 0; i < 10; i++) {
            // 创建消息实例，设置topic和消息内容
            Message msg = new Message(TOPIC_NAME,("Hello RocketMQ Client this is a test message" + i).getBytes(StandardCharsets.UTF_8));
            // 发送消息
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
    }
}
