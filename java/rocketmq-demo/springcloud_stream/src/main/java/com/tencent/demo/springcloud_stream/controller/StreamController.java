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
package com.tencent.demo.springcloud_stream.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.demo.springcloud_stream.config.CustomChannelBinder;
import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StreamController {

    @Autowired
    private CustomChannelBinder channelBinder;

    /**
     * 测试发布简单消息
     */
    @GetMapping("test-simple")
    public String testSimpleStream() {
        Message<JSONObject> message = MessageBuilder.withPayload(JSON.parseObject("{\"key\":\"value\" }"))
                .build();
        channelBinder.sendChannel().send(
                MessageBuilder.withPayload(message)
                        .setHeader(MessageConst.PROPERTY_TAGS, "TAG1") // 设置消息tag
                        .setHeader(MessageConst.PROPERTY_KEYS, "mykey") // 设置消息业务key
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .build());
        return "success";
    }


    /**
     * 测试发布延迟或定时消息
     */
    @GetMapping("test-delay")
    public String testDelayStream() {
        // 设定消息在10秒之后被发送，也可以通过设置消息延迟级别，根据业务进行选择
        long delayTime = System.currentTimeMillis() + 10000;
        Message<String> message = org.springframework.integration.support.MessageBuilder.withPayload("this is new delay message.")
                // 设置延迟级别实现延迟消息 级别支持: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h, 1 = 1s, 2 = 5s 依次类推
//                .setHeader(MessageConst.PROPERTY_DELAY_TIME_LEVEL, 3)
                // 设置定时消息需要指定之后某一时间的时间戳
                .setHeader("__STARTDELIVERTIME", String.valueOf(delayTime))
                .build();
        channelBinder.sendChannel().send(message);
        return "success";
    }


    /**
     * 测试顺序消息
     * 顺序消息需要使用顺序类型的topic才可以（topic支持全局顺序和局部顺序两种顺序类型）
     */
    @GetMapping("test-order")
    public String testOrderStream() {
        for (int i = 0; i < 100; i++) {
            Message<String> message = MessageBuilder.withPayload("this is new order message " + i)
                    .build();
            channelBinder.sendChannel().send(message);
        }
        return "success";
    }
}
