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
 * Description: 消费者2
 */
@Service
@RocketMQMessageListener(
    consumerGroup = "${rocketmq.consumer2.group}",  // 消费组，格式：group名称
    // topic名称，格式：topic名称
    topic = "${rocketmq.consumer2.topic}",
    selectorExpression = "${rocketmq.consumer2.subExpression}", // 订阅表达式, 不配置表示订阅所有消息
    enableMsgTrace = true //打开轨迹
)
public class Tag2Consumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        System.out.println("Tag2Consumer receive message：" + message);
    }
}
