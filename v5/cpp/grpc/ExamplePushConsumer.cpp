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
#include <mutex>
#include <thread>

#include "rocketmq/Logger.h"
#include "rocketmq/PushConsumer.h"

using namespace ROCKETMQ_NAMESPACE;

static const std::string topic = "xxx";
// 填写腾讯云提供的接入地址
static const std::string access_point = "rmq-xxx.rocketmq.xxxtencenttdmq.com:8081";
// 添加配置的ak和sk
static const std::string access_key = "xxx";
static const std::string access_secret = "xxx";
static const std::string group = "group-xxx";

int main(int argc, char *argv[]) {
    auto &logger = getLogger();
    logger.setConsoleLevel(Level::Info);
    logger.setLevel(Level::Info);
    logger.init();

    std::string tag = "*";

    auto listener = [](const Message &message) {
        std::cout << "Received a message[topic=" << message.topic() << ", MsgId=" << message.id() << "]" << std::endl;
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
