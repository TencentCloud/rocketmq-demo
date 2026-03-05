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
 * 固定延迟级别的定时消息，建议用户使用更自由精确的TimerMessageProducer
 * 延迟消息通过设置延迟等级来实现
 * 等级与时间对应关系：
 * 1s、 5s、 10s、 30s、 1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h；
 * 1    2    3     4     5    6   7    8   9    10   11   12  13   14    15    16   17   18
 */
public class DelayMessageProducer {

    /**
     * topic名称
     */
    private static final String TOPIC_NAME = "topic2";

    /**
     * 生产者组名称
     */
    private static final String GROUP_NAME = "group2";

    public static void main(String[] args) throws Exception {
        // 创建消息生产者
        DefaultMQProducer producer = ClientCreater.createProducer(GROUP_NAME);

        int totalMessagesToSend = 5;
        for (int i = 0; i < totalMessagesToSend; i++) {
            Message message = new Message(TOPIC_NAME, ("Hello scheduled message " + i).getBytes());
            // 设置消息延迟等级
            message.setDelayTimeLevel(5);
            // 发送消息
            SendResult sendResult = producer.send(message);
            System.out.println("sendResult = " + sendResult);
        }

        producer.shutdown();
    }
}
