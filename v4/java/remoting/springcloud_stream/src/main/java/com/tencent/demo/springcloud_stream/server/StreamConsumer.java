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

import com.tencent.demo.springcloud_stream.StreamDemoApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

/**
 * 消费消息
 */
@Configuration
public class StreamConsumer {
    private final Logger logger = LoggerFactory.getLogger(StreamDemoApplication.class);

    /**
     * 监听 topicTag1-in-0 binding (配置中的 binding 名称)
     *
     * @return 消费函数
     */
    @Bean
    public Consumer<Message<String>> topicTag1() {
        return message -> logger.info("Receive1: 通过stream收到消息，messageBody = {}", message.getPayload());
    }

    /**
     * 监听 topicTag2-in-0 binding (配置中的 binding 名称)
     *
     * @return 消费函数
     */
    @Bean
    public Consumer<Message<String>> topicTag2() {
        return message -> logger.info("Receive2: 通过stream收到消息，messageBody = {}", message.getPayload());
    }
}
