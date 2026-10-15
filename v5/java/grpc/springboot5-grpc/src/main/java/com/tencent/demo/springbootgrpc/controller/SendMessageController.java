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
package com.tencent.demo.springbootgrpc.controller;

import com.tencent.demo.springbootgrpc.producer.DelayMessageProducer;
import com.tencent.demo.springbootgrpc.producer.FifoMessageProducer;
import com.tencent.demo.springbootgrpc.producer.NormalMessageProducer;
import com.tencent.demo.springbootgrpc.producer.RetryMessageProducer;
import com.tencent.demo.springbootgrpc.producer.TagMessageProducer;
import com.tencent.demo.springbootgrpc.producer.TransactionMessageProducer;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description: 测试发送消息，每个接口对应一个场景
 */
@RestController
public class SendMessageController {

    @Autowired
    private NormalMessageProducer normalMessageProducer;

    @Autowired
    private TagMessageProducer tagMessageProducer;

    @Autowired
    private FifoMessageProducer fifoMessageProducer;

    @Autowired
    private DelayMessageProducer delayMessageProducer;

    @Autowired
    private TransactionMessageProducer transactionMessageProducer;

    @Autowired
    private RetryMessageProducer retryMessageProducer;

    /**
     * 普通消息同步发送
     */
    @GetMapping("/normal/syncSend")
    public String normalSyncSend() {
        normalMessageProducer.syncSend("This is a sync normal message.");
        return "success";
    }

    /**
     * 普通消息异步发送
     */
    @GetMapping("/normal/asyncSend")
    public String normalAsyncSend() {
        normalMessageProducer.asyncSend("This is an async normal message.");
        return "success";
    }

    /**
     * Tag 消息：TagA/TagB 会被 TagMessageConsumer（TagA||TagB）消费，TagC 被过滤
     */
    @GetMapping("/tag/send")
    public String tagSend() {
        tagMessageProducer.send("This is a TagA message.", "TagA");
        tagMessageProducer.send("This is a TagB message.", "TagB");
        tagMessageProducer.send("This is a TagC message.", "TagC");
        return "success";
    }

    /**
     * 顺序消息：同一 messageGroup 内按顺序消费
     */
    @GetMapping("/fifo/send")
    public String fifoSend() {
        for (int i = 0; i < 5; i++) {
            fifoMessageProducer.send("This is a fifo message, index=" + i);
        }
        return "success";
    }

    /**
     * 延迟消息：10 秒后投递
     */
    @GetMapping("/delay/send")
    public String delaySend() {
        delayMessageProducer.send("This is a delay message.", Duration.ofSeconds(10));
        return "success";
    }

    /**
     * 事务消息
     */
    @GetMapping("/transaction/send")
    public String transactionSend() {
        String orderId = UUID.randomUUID().toString();
        transactionMessageProducer.send("This is a transaction message.", orderId);
        return "success";
    }

    /**
     * 重试消息：消费者返回 FAILURE 触发重试
     */
    @GetMapping("/retry/send")
    public String retrySend() {
        retryMessageProducer.send("This is a message to be retried.");
        return "success";
    }
}
