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
package com.tencent.demo.springcloud_stream.producer;

import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 事务消息 - 本地事务执行与回查
 * Bean 名称 streamTransactionListener 需要与 application.yml 中
 * spring.cloud.stream.rocketmq.bindings.txProducer-out-0.producer.transaction-listener 一致。
 */
@Component("streamTransactionListener")
public class StreamTransactionListener implements TransactionListener {

    private final Logger logger = LoggerFactory.getLogger(StreamTransactionListener.class);

    /**
     * 半消息发送成功后执行本地事务
     *
     * @param msg 半消息
     * @param arg 发送时 TRANSACTIONAL_ARGS 消息头的值
     * @return 本地事务状态
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String body = new String(msg.getBody(), StandardCharsets.UTF_8);
        String localTxResult = String.valueOf(arg);
        switch (localTxResult) {
            case "COMMIT":
                logger.info("Transaction: 本地事务成功，提交消息，keys = {}, body = {}", msg.getKeys(), body);
                return LocalTransactionState.COMMIT_MESSAGE;
            case "ROLLBACK":
                logger.info("Transaction: 本地事务失败，回滚消息，keys = {}, body = {}", msg.getKeys(), body);
                return LocalTransactionState.ROLLBACK_MESSAGE;
            case "UNKNOWN":
                logger.info("Transaction: 本地事务状态未知，等待服务端回查，keys = {}, body = {}", msg.getKeys(), body);
                return LocalTransactionState.UNKNOW;
            default:
                logger.error("Transaction: 未识别的本地事务结果 {}，返回 UNKNOW 等待回查，keys = {}", localTxResult, msg.getKeys());
                return LocalTransactionState.UNKNOW;
        }
    }

    /**
     * 服务端回查本地事务状态（executeLocalTransaction 返回 UNKNOW 或没有返回时触发）
     * 实际业务中应根据消息 key（如订单号）查询本地事务表决定提交还是回滚
     *
     * @param msg 半消息
     * @return 本地事务状态
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        logger.info("Transaction: 服务端回查，msgId = {}, keys = {}, 查询本地事务已成功，提交消息", msg.getMsgId(), msg.getKeys());
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
