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
package com.tencent.demo.springcloud_stream.controller;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import org.apache.rocketmq.common.message.MessageConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 事务消息 - 生产者
 * 事务消息需要使用事务类型的topic；txProducer-out-0 在 application.yml 中配置了 producer-type: Trans
 * 和 transaction-listener: streamTransactionListener（见 producer/StreamTransactionListener）。
 */
@RestController
public class TransactionStreamController {

    private final Logger logger = LoggerFactory.getLogger(TransactionStreamController.class);

    private final StreamBridge streamBridge;

    public TransactionStreamController(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * 测试事务消息
     * 依次发送 3 条半消息，通过 TRANSACTIONAL_ARGS 头把"本地事务执行结果"传给 TransactionListener#executeLocalTransaction 的 arg 参数：
     * COMMIT 提交（消费者可见）、ROLLBACK 回滚（消费者不可见）、UNKNOWN 未知（等待服务端回查 checkLocalTransaction）
     */
    @GetMapping("test-transaction")
    public String testTransactionStream() {
        String[] localTxResults = {"COMMIT", "ROLLBACK", "UNKNOWN"};
        List<String> failed = new ArrayList<>();
        for (int i = 0; i < localTxResults.length; i++) {
            String orderId = "order-" + i;
            Message<String> message = MessageBuilder.withPayload("this is new transaction message, orderId = " + orderId)
                    .setHeader(MessageConst.PROPERTY_KEYS, orderId) // 设置消息业务key，回查时用它查询本地事务状态
                    .setHeader(RocketMQConst.USER_TRANSACTIONAL_ARGS, localTxResults[i]) // 作为 executeLocalTransaction 的 arg 参数
                    .build();
            boolean sent = streamBridge.send("txProducer-out-0", message);
            logger.info("Send: 发送事务半消息，orderId = {}, 模拟本地事务结果 = {}, sent = {}", orderId, localTxResults[i], sent);
            if (!sent) {
                failed.add(orderId);
            }
        }
        return failed.isEmpty() ? "success" : "fail: " + failed;
    }
}
