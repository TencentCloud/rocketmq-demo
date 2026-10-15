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
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.SessionCredentialsProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.LitePushConsumer;
import org.apache.rocketmq.client.apis.consumer.OffsetOption;
import org.apache.rocketmq.client.java.exception.LiteSubscriptionQuotaExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteTopicPushConsumer {
    private static final Logger log = LoggerFactory.getLogger(LiteTopicPushConsumer.class);

    private LiteTopicPushConsumer() {
    }

    public static void main(String[] args) throws ClientException, IOException, InterruptedException {
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
        // In most case, you don't need to create too many consumers, singleton pattern is recommended.
        LitePushConsumer litePushConsumer = provider.newLitePushConsumerBuilder()
            .setClientConfiguration(clientConfiguration)
            // Set the consumer group name.
            .setConsumerGroup(consumerGroup)
            // 绑定一级 Topic，后续订阅的 lite topic 都属于该 Topic
            .bindTopic(topic)
            .setMessageListener(messageView -> {
                // Handle the received message and return consume result.
                log.info("Consume message, liteTopic={}, messageId={}", messageView.getLiteTopic().orElse(null),
                    messageView.getMessageId());
                return ConsumeResult.SUCCESS;
            })
            .build();

        try {
            // subscribeLite() 会发起网络请求并校验配额，可能失败，需要检查结果：
            // 1. 网络请求失败，可以稍后重试；
            // 2. 配额校验失败（LiteSubscriptionQuotaExceededException），需要评估配额是否足够，
            //    并及时通过 unsubscribeLite() 取消不再使用的订阅以释放配额。
            // 默认从最新位点开始消费
            litePushConsumer.subscribeLite("lite-topic-0");
            // 从最早位点开始消费
            litePushConsumer.subscribeLite("lite-topic-1", OffsetOption.MIN_OFFSET);
            // 从最后 100 条消息开始消费
            litePushConsumer.subscribeLite("lite-topic-2", OffsetOption.ofTailN(100));
            log.info("Subscribed lite topics={}", litePushConsumer.getLiteTopicSet());
        } catch (LiteSubscriptionQuotaExceededException e) {
            // 1. 评估并提升 lite topic 订阅配额
            // 2. 及时取消不再使用的订阅，例如：litePushConsumer.unsubscribeLite("lite-topic-2");
            log.error("Lite subscription quota exceeded", e);
        } catch (Throwable t) {
            // 稍后重试
            log.error("Failed to subscribe lite topic", t);
        }

        // Block the main thread, no need for production environment.
        Thread.sleep(Long.MAX_VALUE);
        // Close the push consumer when you don't need it anymore.
        litePushConsumer.close();
    }
}
