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
package com.tencent.demo.springboot.controller;

import com.tencent.demo.springboot.producer.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: 测试发送消息
 */
@RestController
public class SendMessageController {

    @Autowired
    private MessageProducer sendMessage;

    /**
     * 发送同步消息
     */
    @GetMapping("/syncSend")
    public String syncSend() {
        sendMessage.syncSend("This is a new message1.", "TAG1");
        sendMessage.syncSend("This is a new message2.", "TAG1");
        sendMessage.syncSend("This is a new message3.", "TAG2");
        sendMessage.syncSend("This is a new message4.", "TAG2");
        return "success";
    }

    /**
     * 发送异步消息
     */
    @GetMapping("/asyncSend")
    public String asyncSend() {
        sendMessage.asyncSend("This is a async message1.", "TAG1");
        sendMessage.asyncSend("This is a async message2.", "TAG1");
        sendMessage.asyncSend("This is a async message3.", "TAG2");
        sendMessage.asyncSend("This is a async message4.", "TAG2");
        return "success";
    }

    /**
     * 发送单向消息
     */
    @GetMapping("/onewaySend")
    public String onewaySend() {
        sendMessage.onewaySend("This is a oneway message1.", "TAG1");
        sendMessage.onewaySend("This is a oneway message2.", "TAG1");
        sendMessage.onewaySend("This is a oneway message3.", "TAG2");
        sendMessage.onewaySend("This is a oneway message4.", "TAG2");
        return "success";
    }

    /**
     * 发送延迟消息
     */
    @GetMapping("/delaySend")
    public String delaySend() {
        sendMessage.delaySend("This is a delay message1.", "TAG1", 2);
        sendMessage.delaySend("This is a delay message2.", "TAG1", 2);
        sendMessage.delaySend("This is a delay message3.", "TAG2", 4);
        sendMessage.delaySend("This is a delay message4.", "TAG2", 4);
        return "success";
    }


    /**
     * 批量发送消息
     */
    @GetMapping("/batchSend")
    public String batchSend() {
        sendMessage.batchSend("This is a batchSend message.", "TAG1");
        sendMessage.batchSend("This is a batchSend message.", "TAG2");
        return "success";
    }

    /**
     * 发送同步顺序消息
     */
    @GetMapping("/syncSendOrderly")
    public String syncSendOrderly() {
        sendMessage.syncSendOrderly("This is a new message1.", "TAG1", "orderId-001");
        sendMessage.syncSendOrderly("This is a new message2.", "TAG1", "orderId-001");
        sendMessage.syncSendOrderly("This is a new message3.", "TAG2", "orderId-001");
        sendMessage.syncSendOrderly("This is a new message4.", "TAG2", "orderId-001");
        return "success";
    }
}
