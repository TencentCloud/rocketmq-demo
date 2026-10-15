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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.demo.rocketmq5.lite.consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.SessionCredentialsProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.consumer.LiteSimpleConsumer;
import org.apache.rocketmq.client.apis.consumer.OffsetOption;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.java.exception.LiteSubscriptionQuotaExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteTopicSimpleConsumer {
    private static final Logger log = LoggerFactory.getLogger(LiteTopicSimpleConsumer.class);

    private LiteTopicSimpleConsumer() {
    }

    public static void main(String[] args) throws ClientException, IOException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        // 添加配置的ak和sk
        String accessKey = "yourAccessKey"; //ak
        String secretKey = "yourSecretKey"; //sk
        SessionCredentialsProvider sessionCredentialsProvider =
            new StaticSessionCredentialsProvider(accessKey, secretKey);

        // 填写腾讯云提供的接入地址
        String endpoints = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8080";
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
            .setEndpoints(endpoints)
            .enableSsl(false)
            .setCredentialProvider(sessionCredentialsProvider)
            .build();
        // 控制台创建的消费组，需选择「Lite Topic 消费」模式并绑定到下面的一级 Topic
        String consumerGroup = "yourConsumerGroup";
        // 控制台创建的「轻量消息」类型的一级 Topic 名称
        String topic = "yourParentTopic";
        Duration awaitDuration = Duration.ofSeconds(5);
        // In most case, you don't need to create too many consumers, singleton pattern is recommended.
        LiteSimpleConsumer consumer = provider.newLiteSimpleConsumerBuilder()
            .setClientConfiguration(clientConfiguration)
            // Set the consumer group name.
            .setConsumerGroup(consumerGroup)
            // 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
            .bindTopic(topic)
            // set await duration for long-polling.
            .setAwaitDuration(awaitDuration)
            .build();

        try {
            // subscribeLite() 会发起网络请求并校验配额，可能失败，需要检查结果
            consumer.subscribeLite("lite-topic-0");
            consumer.subscribeLite("lite-topic-1", OffsetOption.MIN_OFFSET);
            log.info("Subscribed lite topics={}", consumer.getLiteTopicSet());
        } catch (LiteSubscriptionQuotaExceededException e) {
            // 评估并提升 lite topic 订阅配额，并及时 unsubscribeLite() 不再使用的订阅
            log.error("Lite subscription quota exceeded", e);
        } catch (Throwable t) {
            // 稍后重试
            log.error("Failed to subscribe lite topic", t);
        }

        // Max message num for each long polling.
        int maxMessageNum = 16;
        // Set message invisible duration after it is received.
        Duration invisibleDuration = Duration.ofSeconds(15);
        // Receive message, multi-threading is more recommended.
        do {
            final List<MessageView> messages = consumer.receive(maxMessageNum, invisibleDuration);
            log.info("Received {} message(s)", messages.size());
            for (MessageView message : messages) {
                try {
                    log.info("Consume message, liteTopic={}, messageId={}", message.getLiteTopic().orElse(null),
                        message.getMessageId());
                    // 业务处理成功后 ack，否则消息会在不可见时间结束后重新投递
                    consumer.ack(message);
                    log.info("Message is acknowledged successfully, messageId={}", message.getMessageId());
                } catch (Throwable t) {
                    log.error("Message is failed to be acknowledged, messageId={}", message.getMessageId(), t);
                }
            }
        } while (true);
        // Close the simple consumer when you don't need it anymore.
        // consumer.close();
    }
}
