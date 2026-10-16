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
 * 顺序消息 - 消费者
 * orderlyConsumer-in-0 配置了 push.orderly: true，binder 使用 MessageListenerOrderly 按队列串行消费；
 * 消费函数抛异常时 binder 返回 SUSPEND_CURRENT_QUEUE_A_MOMENT，暂停该队列 push.suspend-current-queue-time-millis（默认 1000ms）
 * 后在客户端本地重试，期间该队列的后续消息被阻塞；失败次数达到 push.max-reconsume-times 后进入死信队列 %DLQ%消费组。
 * 注意 binder 的 max-reconsume-times 默认是 0（首次失败就进死信），所以 application.yml 中为该 binding 显式配置了重试次数。
 */
@Configuration
public class OrderlyStreamConsumer {

    private final Logger logger = LoggerFactory.getLogger(OrderlyStreamConsumer.class);

    /**
     * 监听 orderlyConsumer-in-0 binding (配置中的 binding 名称)
     *
     * @return 消费函数
     */
    @Bean
    public Consumer<Message<String>> orderlyConsumer() {
        String queueIdHeader = RocketMQMessageConverterSupport.toRocketHeaderKey(RocketMQConst.Headers.QUEUE_ID);
        return message -> logger.info("Orderly: {} 收到顺序消息，queueId = {}, messageBody = {}",
                Thread.currentThread().getName(), message.getHeaders().get(queueIdHeader), message.getPayload());
    }
}
