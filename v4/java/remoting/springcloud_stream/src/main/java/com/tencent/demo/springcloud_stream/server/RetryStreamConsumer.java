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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import com.alibaba.cloud.stream.binder.rocketmq.support.RocketMQMessageConverterSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * 重试消息 - 消费者
 * <p>
 * 监听 retryConsumer-in-0 binding，消费函数抛出异常即触发 RocketMQ 服务端重试（集群模式）：
 * binder 捕获异常后给 RocketMQ 返回 RECONSUME_LATER，消息进入重试队列 %RETRY%消费组，
 * 按重试次数递增延迟后重新投递；超过 max-reconsume-times 后进入死信队列 %DLQ%消费组。
 * <p>
 * 两种重试的区别（配置见 application.yml 中 retryConsumer-in-0）：
 * <ul>
 *   <li>Spring Cloud Stream 本地重试：spring.cloud.stream.bindings.xxx.consumer.max-attempts（默认 3），
 *       在同一个消费线程里立即重放，消息不回到服务端；本示例设为 1 关闭，便于观察服务端重试</li>
 *   <li>RocketMQ 服务端重试：spring.cloud.stream.rocketmq.bindings.xxx.consumer.push.max-reconsume-times，
 *       注意 binder 的默认值是 0（即首次失败直接进死信队列，而不是 RocketMQ 客户端默认的 16 次），
 *       需要重试时必须显式配置</li>
 * </ul>
 * 如果给该 binding 配置了 error-handler-definition 或订阅了它的错误通道，异常会被吞掉，binder 返回消费成功，不会触发服务端重试。
 * <p>
 * binder 不会把 RocketMQ 的 reconsumeTimes 放进 Spring 消息头（RocketMQMessageConverterSupport#convertMessage2Spring
 * 只转换 KEYS/TAGS/TOPIC/MESSAGE_ID 等字段，RECONSUME_TIME 属于系统属性会被过滤），
 * 而消息 ID（ROCKET_MQ_MESSAGE_ID）在重试时保持不变，所以这里按消息 ID 在内存里统计投递次数。
 * 该计数只在单个进程内有效、重启丢失，仅用于演示。
 */
@Configuration
public class RetryStreamConsumer {

    private final Logger logger = LoggerFactory.getLogger(RetryStreamConsumer.class);

    /**
     * 消息 ID -> 已投递次数（仅演示用）
     */
    private final Map<String, AtomicInteger> deliveryTimes = new ConcurrentHashMap<>();

    /**
     * 监听 retryConsumer-in-0 binding (配置中的 binding 名称)
     *
     * @return 消费函数
     */
    @Bean
    public Consumer<Message<String>> retryConsumer() {
        String msgIdHeader = RocketMQMessageConverterSupport.toRocketHeaderKey(RocketMQConst.Headers.MESSAGE_ID);
        return message -> {
            Object msgId = message.getHeaders().get(msgIdHeader);
            if (msgId == null) {
                throw new IllegalStateException("消息头缺少 " + msgIdHeader + "，无法统计重试次数");
            }
            int times = deliveryTimes.computeIfAbsent(msgId.toString(), k -> new AtomicInteger()).incrementAndGet();
            logger.info("Retry: 收到消息，msgId = {}, 第 {} 次投递（已重试 {} 次），messageBody = {}",
                    msgId, times, times - 1, message.getPayload());
            // 模拟业务处理失败：抛出异常即触发重试，达到最大重试次数后进入死信队列 %DLQ%retry-consumer-group
            throw new IllegalStateException("模拟消费失败，msgId = " + msgId + ", 第 " + times + " 次投递");
        };
    }
}
