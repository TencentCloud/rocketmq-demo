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
package com.tencent.demo.springbootgrpc.producer;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.apache.rocketmq.client.common.Pair;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.support.RocketMQHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Description: 事务消息生产者，Topic 类型需为「事务」。
 * <p>
 * starter 2.3.6 没有「执行本地事务」回调：先发送半事务消息，再由业务代码执行本地事务并 commit/rollback；
 * 若未提交（如进程崩溃），服务端会调用 {@link TransactionMessageChecker} 回查。
 */
@Service
public class TransactionMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(TransactionMessageProducer.class);

    /**
     * 自定义消息属性，回查时据此查询本地事务状态
     */
    public static final String ORDER_ID_PROPERTY = "OrderId";

    @Value("${demo.rocketmq.transaction-topic}")
    private String topic;

    @Autowired
    private RocketMQClientTemplate rocketMQClientTemplate;

    public void send(String payload, String orderId) {
        Message<String> message = MessageBuilder.withPayload(payload)
            .setHeader(RocketMQHeaders.KEYS, orderId)
            // 非系统 header 会作为消息属性发送，回查时可通过 messageView.getProperties() 读取
            .setHeader(ORDER_ID_PROPERTY, orderId)
            .build();
        // 发送半事务消息，提交前对消费者不可见
        Pair<SendReceipt, Transaction> pair = rocketMQClientTemplate.sendTransactionMessage(topic, message);
        log.info("Send half message successfully, topic={}, messageId={}", topic,
            pair.getSendReceipt().getMessageId());
        Transaction transaction = pair.getTransaction();
        try {
            // 执行本地事务，根据结果提交或回滚
            if (executeLocalTransaction(orderId)) {
                transaction.commit();
                log.info("Transaction committed, orderId={}", orderId);
            } else {
                transaction.rollback();
                log.info("Transaction rolled back, orderId={}", orderId);
            }
        } catch (ClientException e) {
            // commit/rollback 失败时由服务端回查 TransactionMessageChecker 决定最终状态
            log.error("Failed to end transaction, orderId={}", orderId, e);
        }
    }

    /**
     * 模拟本地事务（如写订单表），实际业务中替换为数据库操作
     */
    private boolean executeLocalTransaction(String orderId) {
        log.info("Execute local transaction, orderId={}", orderId);
        return true;
    }
}
