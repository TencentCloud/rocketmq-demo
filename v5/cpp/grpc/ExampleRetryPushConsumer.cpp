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
#include <chrono>
#include <iostream>
#include <thread>

#include "rocketmq/Logger.h"
#include "rocketmq/PushConsumer.h"

using namespace ROCKETMQ_NAMESPACE;

// 重试消息示例：监听器返回 ConsumeResult::FAILURE 即视为消费失败，消息会在退避时间后重新投递。
// 最大重试次数由消费组的重试策略决定（控制台「消费组」配置中设置），超过最大重试次数后消息进入死信队列。
// 注意：
// 1. 生产者的 withMaxAttempts 是「发送失败」时的重试次数，与这里的消费重试无关。
// 2. C++ SDK 不会捕获监听器抛出的异常，业务异常请在监听器内自行 catch 并返回 FAILURE。

// 控制台创建的「普通消息」类型的 Topic
static const std::string topic = "xxx";
// 填写腾讯云提供的接入地址（控制台实例详情页获取）
static const std::string access_point = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8081";
// 添加配置的ak和sk（控制台「角色管理」中创建角色后获取）
static const std::string access_key = "xxx";
static const std::string access_secret = "xxx";
// 控制台创建的消费组，并订阅上面的 Topic；最大重试次数在该消费组的重试策略中配置
static const std::string group = "group-xxx";
// 第几次投递时返回 SUCCESS，用于演示前几次失败触发重试
static const std::uint16_t success_attempt = 3;

int main(int argc, char *argv[]) {
    auto &logger = getLogger();
    logger.setConsoleLevel(Level::Info);
    logger.setLevel(Level::Info);
    logger.init();

    std::string tag = "*";

    auto listener = [](const Message &message) {
        // extension().delivery_attempt 表示当前是第几次投递，首次投递为 1，每重试一次加 1
        std::uint16_t delivery_attempt = message.extension().delivery_attempt;
        std::cout << "Received a message[topic=" << message.topic() << ", MsgId=" << message.id()
                  << ", DeliveryAttempt=" << delivery_attempt << "]" << std::endl;
        if (delivery_attempt < success_attempt) {
            // 返回 FAILURE 触发重试
            std::cout << "Consume failed, MsgId=" << message.id() << " will be redelivered" << std::endl;
            return ConsumeResult::FAILURE;
        }
        // 返回 SUCCESS 确认消费成功，不再重试
        std::cout << "Consume success, MsgId=" << message.id() << std::endl;
        return ConsumeResult::SUCCESS;
    };

    CredentialsProviderPtr credentials_provider;
    if (!access_key.empty() && !access_secret.empty()) {
        credentials_provider = std::make_shared<StaticCredentialsProvider>(access_key, access_secret);
    }

    // In most case, you don't need to create too many consumers, singletion pattern is recommended.
    auto push_consumer = PushConsumer::newBuilder()
            .withGroup(group)
            .withConfiguration(Configuration::newBuilder()
                                       .withEndpoints(access_point)
                                       .withRequestTimeout(std::chrono::seconds(3))
                                       .withCredentialsProvider(credentials_provider)
                                       .withSsl(false)
                                       .build())
            .withConsumeThreads(4)
            .withListener(listener)
            .subscribe(topic, tag)
            .build();

    std::this_thread::sleep_for(std::chrono::minutes(30));

    return EXIT_SUCCESS;
}
