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
package com.tencent.demo.springboot.producer;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 消息生产者
 */
@Service
public class MessageProducer {

    // 使用topic名称
    @Value("${rocketmq.producer1.topic}")
    private String topic;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 同步发送
     *
     * @param message 消息内容
     * @param tags    订阅tags
     */
    public void syncSend(String message, String tags) {
        // springboot不支持使用header传递tags，根据要求，需要在topic后进行拼接 formats: `topicName:tags`，不拼接标识无tag
        String destination = StringUtils.isBlank(tags) ? topic : topic + ":" + tags;
        // object消息类型
        SendResult sendResult = rocketMQTemplate.syncSend(destination,
                MessageBuilder.withPayload(message)
                        .setHeader(MessageConst.PROPERTY_KEYS, "yourKey")   // 指定业务key
                        .build());
        System.out.printf("syncSend1 to topic %s sendResult=%s %n", topic, sendResult);
    }


    /**
     * 异步发送消息
     *
     * @param message 消息内容
     * @param tags    订阅tags
     */
    public void asyncSend(String message, String tags) {
        // springboot不支持使用header传递tags，根据要求，需要在topic后进行拼接 formats: `topicName:tags`，不拼接标识无tag
        String destination = StringUtils.isBlank(tags) ? topic : topic + ":" + tags;
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult var1) {
                System.out.printf("async onSuccess SendResult=%s %n", var1);
            }

            @Override
            public void onException(Throwable var1) {
                System.out.printf("async onException Throwable=%s %n", var1);
            }

        });
    }


    /**
     * 单选发送消息
     *
     * @param message 消息内容
     * @param tags    订阅tags
     */
    public void onewaySend(String message, String tags) {
        // springboot不支持使用header传递tags，根据要求，需要在topic后进行拼接 formats: `topicName:tags`，不拼接标识无tag
        String destination = StringUtils.isBlank(tags) ? topic : topic + ":" + tags;
        rocketMQTemplate.sendOneWay(destination, message);
    }


    /**
     * 发送延迟消息
     *
     * @param message    消息内容
     * @param tags       订阅tags
     * @param delayLevel 消息延迟级别
     *                   延迟等级与时间对应关系：
     *                   1s、 5s、 10s、 30s、 1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h；
     *                   1    2    3     4     5    6   7    8   9    10   11   12  13   14    15    16   17   18
     */
    public void delaySend(String message, String tags, int delayLevel) {
        // springboot不支持使用header传递tags，根据要求，需要在topic后进行拼接 formats: `topicName:tags`，不拼接标识无tag
        String destination = StringUtils.isBlank(tags) ? topic : topic + ":" + tags;
        SendResult sendResult = rocketMQTemplate.syncSend(
                destination,
                MessageBuilder.withPayload(message).build(),
                5000,
                delayLevel);
        System.out.printf("DelaySend to topic %s sendResult=%s %n", topic, sendResult);
    }


    /**
     * 批量发送消息
     *
     * @param message 消息内容
     * @param tags    订阅tags
     */
    public void batchSend(String message, String tags) {
        String destination = StringUtils.isBlank(tags) ? topic : topic + ":" + tags;
        List<Message<String>> messageList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messageList.add(MessageBuilder.withPayload(message + i).
                    setHeader(RocketMQHeaders.KEYS, "KEY_" + i).build());
        }
        SendResult sendResult = rocketMQTemplate.syncSend(destination, messageList, 60000);
        System.out.printf("DelaySend to topic %s sendResult=%s %n", topic, sendResult);
    }

}
