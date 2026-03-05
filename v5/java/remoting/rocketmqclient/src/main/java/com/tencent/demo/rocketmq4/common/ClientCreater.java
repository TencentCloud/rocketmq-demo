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
package com.tencent.demo.rocketmq4.common;

import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;

/**
 * Description: 创建客户端
 * 生产者或消费者
 *
 * <p>【说明】此目录（v5/remoting）下的代码与 v4/remoting 完全相同。
 * 腾讯云 RocketMQ 5.x 同时支持两种接入协议：
 * <ul>
 *   <li><b>gRPC 协议</b>（推荐）：使用 rocketmq-client-java 5.x SDK，见 v5/grpc 目录，
 *       功能最全，支持任意精度延迟消息、FIFO 消息组等新特性。</li>
 *   <li><b>Remoting 协议</b>（兼容）：使用 rocketmq-client 4.x SDK 连接 5.x Broker，迁移成本低。</li>
 * </ul>
 * 新项目建议优先选用 gRPC 协议（v5/grpc 目录）。
 */
public class ClientCreater {

    /**
     * nameserver地址
     */
    public static final String NAMESERVER = "腾讯云页面提供的接入点";

    /**
     * accessKey  对应角色密钥
     */
    public static final String ACCESS_KEY = "ak";

    /**
     * secretKey  对应角色名称
     */
    public static final String SECRET_KEY = "sk";

    /**
     * 创建消息生产者
     *
     * @param groupName 生产者组名称
     * @return producer
     * @throws MQClientException 客户端异常
     */
    public static DefaultMQProducer createProducer(String groupName) throws MQClientException {
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer(groupName,
            // ACL权限
            new AclClientRPCHook(new SessionCredentials(ACCESS_KEY, SECRET_KEY)), true, null);
        // 设置NameServer的地址
        producer.setNamesrvAddr(NAMESERVER);
        // 启动Producer实例
        producer.start();
        return producer;
    }

    /**
     * 创建消息消费(push)
     *
     * @param groupName 消费者组名称
     * @return pushConsumer
     */
    public static DefaultMQPushConsumer createPushConsumer(String groupName) {
        // 实例化消费者
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer(groupName,
            new AclClientRPCHook(new SessionCredentials(ACCESS_KEY, SECRET_KEY)),
            new AllocateMessageQueueAveragely(), true, null);
        // 设置NameServer的地址
        pushConsumer.setNamesrvAddr(NAMESERVER);
        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        return pushConsumer;
    }

    /**
     * 创建消息消费(pull)
     *
     * @param groupName 消费者组名称
     * @return pushConsumer
     */
    public static DefaultLitePullConsumer createPullConsumer(String groupName) {
        // 实例化消费者
        DefaultLitePullConsumer pullConsumer = new DefaultLitePullConsumer(groupName,
            new AclClientRPCHook(new SessionCredentials(ACCESS_KEY, SECRET_KEY)));
        // 设置NameServer的地址
        pullConsumer.setNamesrvAddr(NAMESERVER);
        pullConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        pullConsumer.setAutoCommit(false);
        pullConsumer.setEnableMsgTrace(true);
        pullConsumer.setCustomizedTraceTopic(null);
        return pullConsumer;
    }
}