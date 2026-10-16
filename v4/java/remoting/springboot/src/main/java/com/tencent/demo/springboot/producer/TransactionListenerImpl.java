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

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description: 事务消息本地事务监听器
 *
 * <p>@RocketMQTransactionListener 默认绑定到名为 rocketMQTemplate 的 bean，一个 RocketMQTemplate 只能绑定一个监听器。
 * <ul>
 *     <li>executeLocalTransaction：半消息发送成功后执行本地事务，返回 COMMIT 消息投递给消费者，ROLLBACK 消息丢弃，
 *     UNKNOWN 等待 Broker 回查</li>
 *     <li>checkLocalTransaction：本地事务状态为 UNKNOWN（或未及时返回）时，Broker 回查本地事务状态</li>
 * </ul>
 */
@RocketMQTransactionListener
public class TransactionListenerImpl implements RocketMQLocalTransactionListener {

    private final AtomicInteger transactionIndex = new AtomicInteger(0);

    // 模拟本地事务状态存储，实际业务中应查询数据库等持久化存储
    private final ConcurrentHashMap<String, RocketMQLocalTransactionState> localTrans = new ConcurrentHashMap<>();

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String transId = (String) msg.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
        // 模拟本地事务结果：依次返回 COMMIT、ROLLBACK、UNKNOWN
        int status = transactionIndex.getAndIncrement() % 3;
        RocketMQLocalTransactionState state;
        if (status == 0) {
            state = RocketMQLocalTransactionState.COMMIT;
        } else if (status == 1) {
            state = RocketMQLocalTransactionState.ROLLBACK;
        } else {
            state = RocketMQLocalTransactionState.UNKNOWN;
        }
        localTrans.put(transId, state);
        System.out.printf("executeLocalTransaction transactionId=%s, state=%s %n", transId, state);
        return state;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        String transId = (String) msg.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
        RocketMQLocalTransactionState state = localTrans.get(transId);
        if (state == null) {
            // 查不到本地事务记录（例如应用重启导致内存数据丢失），返回 UNKNOWN 等待下次回查
            System.out.printf("checkLocalTransaction transactionId=%s, local transaction not found, return UNKNOWN %n", transId);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        if (state == RocketMQLocalTransactionState.UNKNOWN) {
            // 模拟回查时本地事务已执行成功
            state = RocketMQLocalTransactionState.COMMIT;
            localTrans.put(transId, state);
        }
        System.out.printf("checkLocalTransaction transactionId=%s, state=%s %n", transId, state);
        return state;
    }
}
