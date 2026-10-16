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

import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Description: 事务消息生产者
 *
 * <p>事务消息使用 rocketMQTemplate 的生产者（生产者组为 rocketmq.producer.group）发送，
 * 本地事务的执行与回查由 {@link TransactionListenerImpl} 处理。
 * <p>腾讯云上需要先创建事务消息类型的 Topic，名称与 application.yml 中 rocketmq.transaction.topic 一致。
 */
@Service
public class TransactionProducer {

    // 需要使用topic全称，所以进行topic名称的拼接，也可以自己设置  格式：namespace全称%topic名称
    @Value("${rocketmq.namespace}%${rocketmq.transaction.topic}")
    private String topic;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送事务消息
     *
     * @param message       消息内容
     * @param transactionId 业务事务ID，executeLocalTransaction/checkLocalTransaction 中通过它查询本地事务状态
     */
    public void sendMessageInTransaction(String message, String transactionId) {
        Message<String> msg = MessageBuilder.withPayload(message)
                .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)  // 自定义属性，回查时可取回
                .setHeader(RocketMQHeaders.KEYS, transactionId)            // 指定业务key，便于控制台查询
                .build();
        // 第三个参数 arg 会原样传给 executeLocalTransaction，这里不需要传
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(topic, msg, null);
        System.out.printf("sendMessageInTransaction to topic %s sendStatus=%s localTransactionState=%s msgId=%s %n",
                topic, sendResult.getSendStatus(), sendResult.getLocalTransactionState(), sendResult.getMsgId());
    }
}
