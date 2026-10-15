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

package com.tencent.demo.rocketmq5.lite.producer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.SessionCredentialsProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.java.exception.LiteTopicQuotaExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteMessageSyncProducer {
    private static final Logger log = LoggerFactory.getLogger(LiteMessageSyncProducer.class);

    private LiteMessageSyncProducer() {
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
        // 控制台创建的「轻量消息」类型的一级 Topic 名称
        String topic = "yourParentTopic";
        // In most case, you don't need to create too many producers, singleton pattern is recommended.
        final Producer producer = provider.newProducerBuilder()
            .setClientConfiguration(clientConfiguration)
            // Set the topic name(s), which is optional but recommended. It makes producer could prefetch the topic
            // route before message publishing.
            .setTopics(topic)
            // May throw {@link ClientException} if the producer is not initialized.
            .build();
        for (int i = 0; i < 10; i++) {
            // 每条消息可以指定不同的 lite topic（二级 Topic），无需提前在控制台创建
            String liteTopic = "lite-topic-" + i;
            // Define your message body.
            byte[] body = ("This is a lite message for Apache RocketMQ, liteTopic=" + liteTopic)
                .getBytes(StandardCharsets.UTF_8);
            final Message message = provider.newMessageBuilder()
                // Set topic for the current message.
                .setTopic(topic)
                // Key(s) of the message, another way to mark message besides message id.
                .setKeys("yourMessageKey-" + i)
                // 设置 lite topic。注意：不能与 messageGroup、deliveryTimestamp、priority 同时使用
                .setLiteTopic(liteTopic)
                .setBody(body)
                .build();
            try {
                final SendReceipt sendReceipt = producer.send(message);
                log.info("Send message successfully, liteTopic={}, messageId={}", liteTopic,
                    sendReceipt.getMessageId());
            } catch (LiteTopicQuotaExceededException e) {
                // lite topic 数量超过配额，需要评估并提升 lite topic 资源上限
                log.error("Lite topic quota exceeded, liteTopic={}", liteTopic, e);
            } catch (Throwable t) {
                log.error("Failed to send message, liteTopic={}", liteTopic, t);
            }
        }
        // Close the producer when you don't need it anymore.
        producer.close();
    }
}
