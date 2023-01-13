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
 */
public class ClientCreater {

    /**
     * namespace名称
     */
    public static final String NAMESPACE = "namespace";

    /**
     * nameserver地址
     */
    public static final String NAMESERVER = "endpoint";

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
        DefaultMQProducer producer = new DefaultMQProducer(NAMESPACE, groupName,
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
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer(NAMESPACE,
                groupName,
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
        DefaultLitePullConsumer pullConsumer = new DefaultLitePullConsumer(NAMESPACE,
                groupName,
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