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

// 控制台创建的「普通消息」类型的 Topic
static const std::string topic = "xxx";
// 填写腾讯云提供的接入地址（控制台实例详情页获取）
static const std::string access_point = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8081";
// 添加配置的ak和sk（控制台「角色管理」中创建角色后获取）
static const std::string access_key = "xxx";
static const std::string access_secret = "xxx";
// 控制台创建的消费组，并订阅上面的 Topic
static const std::string group = "group-xxx";

int main(int argc, char *argv[]) {
    auto &logger = getLogger();
    logger.setConsoleLevel(Level::Info);
    logger.setLevel(Level::Info);
    logger.init();

    // tag 过滤表达式：多个 tag 用 || 分隔，* 表示订阅全部
    // 同一消费组内所有消费者的订阅关系（Topic + tag 表达式）必须保持一致
    std::string tag = "TagA||TagB";

    auto listener = [](const Message &message) {
        std::cout << "Received a message[topic=" << message.topic() << ", tag=" << message.tag()
                  << ", MsgId=" << message.id() << "]" << std::endl;
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
            // 按 tag 过滤，只会收到 tag 为 TagA 或 TagB 的消息
            .subscribe(topic, FilterExpression(tag, ExpressionType::TAG))
            .build();

    std::this_thread::sleep_for(std::chrono::minutes(30));

    return EXIT_SUCCESS;
}
