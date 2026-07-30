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

    # ---- 消费组 A ----
    # 控制台创建时需选择「Lite Topic 消费」模式并绑定到正确的 Topic
    consumer_group_a = "ConsumerGroupA"
    consumer_a = LitePushConsumer(
        config, consumer_group_a, bind_topic, LiteTopicMessageListener()
    )

    # ---- 消费组 B ----
    consumer_group_b = "ConsumerGroupB"
    consumer_b = LitePushConsumer(
        config, consumer_group_b, bind_topic, LiteTopicMessageListener()
    )

    consumers = [consumer_a, consumer_b]

    try:
        # 启动消费组 A，订阅 lite-test-0 ~ lite-test-2
        consumer_a.startup()
        consumer_a.subscribe_lite("lite-test-0")
        consumer_a.subscribe_lite("lite-test-1")
        consumer_a.subscribe_lite("lite-test-2")
        print(f"[{consumer_group_a}] started, subscribed: [lite-test-0, lite-test-1, lite-test-2]")

        # 启动消费组 B，订阅 lite-test-3 ~ lite-test-5
        consumer_b.startup()
        consumer_b.subscribe_lite("lite-test-3")
        consumer_b.subscribe_lite("lite-test-4")
        consumer_b.subscribe_lite("lite-test-5")
        print(f"[{consumer_group_b}] started, subscribed: [lite-test-3, lite-test-4, lite-test-5]")

        # 也可以继续订阅更多 lite topic
        # consumer_b.subscribe_lite("lite-test-6")

        print("\n[Main] All consumers started. Press Enter to stop.")
        input("Please Enter to Stop the Application.\r\n")
    except Exception as e:
        print(f"Startup raise exception: {e}")
    finally:
        for c in consumers:
            c.shutdown()
            print(f"{c} shutdown.")
