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

import org.apache.rocketmq.client.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;
import org.apache.rocketmq.client.core.RocketMQTransactionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description: 事务消息回查。
 * <p>
 * starter 启动时会把带 @RocketMQTransactionListener 的 Bean 设置为 rocketMQClientTemplate 生产者的 TransactionChecker，
 * 半事务消息长时间未 commit/rollback 时，服务端调用 check 查询本地事务状态。
 */
@RocketMQTransactionListener
public class TransactionMessageChecker implements RocketMQTransactionChecker {
    private static final Logger log = LoggerFactory.getLogger(TransactionMessageChecker.class);

    @Override
    public TransactionResolution check(MessageView messageView) {
        String orderId = messageView.getProperties().get(TransactionMessageProducer.ORDER_ID_PROPERTY);
        log.info("Receive transaction check, messageId={}, orderId={}", messageView.getMessageId(), orderId);
        if (orderId == null) {
            log.warn("OrderId property is missing, rollback, messageId={}", messageView.getMessageId());
            return TransactionResolution.ROLLBACK;
        }
        // 实际业务中按 orderId 查询本地事务（如订单表）是否已提交：
        // 已提交返回 COMMIT，已失败返回 ROLLBACK，仍在处理中返回 UNKNOWN 等待下次回查
        return TransactionResolution.COMMIT;
    }
}
