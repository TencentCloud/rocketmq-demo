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
package com.tencent.demo.springcloud_stream.server;

import java.util.function.Consumer;

import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * 事务消息 - 消费者
 * 只会收到已提交（COMMIT 或回查后提交）的事务消息，回滚的消息不可见。
 */
@Configuration
public class TransactionStreamConsumer {

    private final Logger logger = LoggerFactory.getLogger(TransactionStreamConsumer.class);

    /**
     * 监听 txConsumer-in-0 binding (配置中的 binding 名称)
     *
     * @return 消费函数
     */
    @Bean
    public Consumer<Message<String>> txConsumer() {
        String keysHeader = RocketMQMessageConverterSupport.toRocketHeaderKey(RocketMQConst.Headers.KEYS);
        return message -> logger.info("Transaction: 收到已提交的事务消息，keys = {}, messageBody = {}",
                message.getHeaders().get(keysHeader), message.getPayload());
    }
}
