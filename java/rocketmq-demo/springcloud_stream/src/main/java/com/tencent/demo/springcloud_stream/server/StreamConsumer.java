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

import com.tencent.demo.springcloud_stream.StreamDemoApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

/**
 * 消费消息
 */
@Service
public class StreamConsumer {
    private final Logger logger = LoggerFactory.getLogger(StreamDemoApplication.class);

    /**
     * 监听channel (配置中的channel 名称)
     *
     * @param messageBody 消息内容
     */
    @StreamListener("Topic-test1")
    public void receive(String messageBody) {
        logger.info("Receive1: 通过stream收到消息，messageBody = {}", messageBody);
    }

    /**
     * 监听channel (配置中的channel 名称)
     *
     * @param messageBody 消息内容
     */
    @StreamListener("Topic-test2")
    public void receive2(String messageBody) {
        logger.info("Receive2: 通过stream收到消息，messageBody = {}", messageBody);
    }
}
