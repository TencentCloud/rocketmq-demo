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
package com.tencent.demo.springboot.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * Description: 事务消息消费者
 *
 * <p>只有本地事务提交（COMMIT）后的事务消息才会投递给消费者，回滚（ROLLBACK）的消息不会被消费。
 * <p>腾讯云上需要先创建事务消息类型的 Topic 和消费组，名称与 application.yml 中 rocketmq.transaction 配置一致。
 */
@Service
@RocketMQMessageListener(
    consumerGroup = "${rocketmq.namespace}%${rocketmq.transaction.group}",  // 消费组，格式：namespace全称%group名称
    // 需要使用topic全称，所以进行topic名称的拼接，也可以自己设置  格式：namespace全称%topic名称
    topic = "${rocketmq.namespace}%${rocketmq.transaction.topic}",
    selectorExpression = "*", // 订阅表达式, * 表示订阅所有消息
    enableMsgTrace = true //打开轨迹
)
public class TransactionConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        System.out.println("TransactionConsumer receive message：" + message);
    }
}
