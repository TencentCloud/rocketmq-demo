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

import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * Description: 广播消息消费者
 *
 * <p>广播模式下，同一消费组内的每个消费者实例都会收到 Topic 的全部消息（集群模式下同组实例分摊消息）。
 * 可启动多个本应用实例（修改 server.port）观察每个实例都打印了相同的消息。
 * <p>广播模式下消费失败不会重试，消费位点保存在客户端本地。
 * <p>腾讯云上需要先创建 Topic（普通消息类型）和消费组，名称与 application.yml 中 rocketmq.broadcast 配置一致。
 */
@Service
@RocketMQMessageListener(
    consumerGroup = "${rocketmq.namespace}%${rocketmq.broadcast.group}",  // 消费组，格式：namespace全称%group名称
    // 需要使用topic全称，所以进行topic名称的拼接，也可以自己设置  格式：namespace全称%topic名称
    topic = "${rocketmq.namespace}%${rocketmq.broadcast.topic}",
    selectorExpression = "*", // 订阅表达式, * 表示订阅所有消息
    enableMsgTrace = true, //打开轨迹
    messageModel = MessageModel.BROADCASTING // 设置为广播消费
)
public class BroadcastConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        System.out.println("BroadcastConsumer receive message：" + message);
    }
}
