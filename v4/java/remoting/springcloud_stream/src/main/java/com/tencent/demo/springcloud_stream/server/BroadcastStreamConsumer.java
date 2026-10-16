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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * 广播消息 - 消费者
 * broadcastConsumer-in-0 配置了 message-model: BROADCASTING，同一消费组的每个实例都会收到全部消息；
 * 广播模式下消费位点保存在客户端本地，消费失败不会重试（RocketMQ 客户端直接丢弃并打印 warn 日志）。
 */
@Configuration
public class BroadcastStreamConsumer {

    private final Logger logger = LoggerFactory.getLogger(BroadcastStreamConsumer.class);

    /**
     * 监听 broadcastConsumer-in-0 binding (配置中的 binding 名称)
     *
     * @return 消费函数
     */
    @Bean
    public Consumer<Message<String>> broadcastConsumer() {
        return message -> logger.info("Broadcast: 收到广播消息，messageBody = {}", message.getPayload());
    }
}
