# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from rocketmq import (ClientConfiguration, ConsumeResult, Credentials,
                      LitePushConsumer, Message, MessageListener)


class LiteTopicMessageListener(MessageListener):

    def consume(self, message: Message) -> ConsumeResult:
        print(f"consume message, lite_topic={message.lite_topic}, "
              f"message_id={message.message_id}, body={message.body}")
        # 消费成功返回 SUCCESS
        return ConsumeResult.SUCCESS
        # 消费失败返回 FAILURE，该消息将会被重新消费
        # return ConsumeResult.FAILURE


if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    config = ClientConfiguration(endpoints, credentials)

    # 控制台创建的「轻量消息」类型的一级 Topic 名称
    bind_topic = "parentTopic"
    # 同一个消费组，控制台创建时需选择「Lite Topic 消费」模式并绑定到正确的 Topic
    consumer_group = "ConsumerGroup"

    # 核心特性：同一消费组下，不同客户端可以各自订阅不同的 lite topic
    # 这是 Lite Topic 与普通 Topic 最大的区别——普通 Topic 要求同一 Group 下订阅关系一致

    # 客户端 1：仅订阅 lite-test-0
    client1 = LitePushConsumer(
        config, consumer_group, bind_topic, LiteTopicMessageListener()
    )

    # 客户端 2：仅订阅 lite-test-1
    client2 = LitePushConsumer(
        config, consumer_group, bind_topic, LiteTopicMessageListener()
    )

    # 客户端 3：订阅 lite-test-2 和 lite-test-3
    client3 = LitePushConsumer(
        config, consumer_group, bind_topic, LiteTopicMessageListener()
    )

    consumers = [client1, client2, client3]

    try:
        client1.startup()
        client1.subscribe_lite("lite-test-0")
        print(f"[{consumer_group}] client1 started, subscribed: [lite-test-0]")

        client2.startup()
        client2.subscribe_lite("lite-test-1")
        print(f"[{consumer_group}] client2 started, subscribed: [lite-test-1]")

        client3.startup()
        client3.subscribe_lite("lite-test-2")
        client3.subscribe_lite("lite-test-3")
        print(f"[{consumer_group}] client3 started, subscribed: [lite-test-2, lite-test-3]")

        print("\n[Main] Same group, different subscriptions — each client consumes its own lite topic(s).")
        print("[Main] This is NOT possible with normal topics. Press Enter to stop.")
        input("Please Enter to Stop the Application.\r\n")
    except Exception as e:
        print(f"Startup raise exception: {e}")
    finally:
        for c in consumers:
            c.shutdown()
            print(f"{c} shutdown.")
