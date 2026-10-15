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
                      FilterExpression, Message, MessageListener, PushConsumer)

# 示例：前两次投递返回 FAILURE，第三次投递时返回 SUCCESS，用来观察重试过程
SUCCEED_AT_ATTEMPT = 3


class RetryMessageListener(MessageListener):

    def consume(self, message: Message) -> ConsumeResult:
        # delivery_attempt 表示这是该消息第几次投递，首次投递为 1
        delivery_attempt = message.delivery_attempt
        print(f"consume message, message_id={message.message_id}, "
              f"delivery_attempt={delivery_attempt}, body={message.body}")
        if delivery_attempt is None or delivery_attempt < SUCCEED_AT_ATTEMPT:
            # 返回 FAILURE（或在这里抛出异常）会触发消费重试，消息在退避时间后重新投递
            print(f"consume failed, wait for retry, message_id={message.message_id}")
            return ConsumeResult.FAILURE
        print(f"consume success after retry, message_id={message.message_id}")
        return ConsumeResult.SUCCESS


if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    # 控制台创建的「普通消息」类型的 Topic
    topic = "topic1"
    # 控制台创建的消费组。最大重试次数由消费组的重试策略决定（控制台消费组配置），
    # 超过最大重试次数仍消费失败的消息会进入死信队列
    consumer_group = "ConsumerGroup"

    # 注意：生产者发送失败时的重试（发送重试）与这里的消费重试无关
    push_consumer = PushConsumer(
        config, consumer_group, RetryMessageListener(),
        {topic: FilterExpression()}
    )

    try:
        push_consumer.startup()
        print(f" [Consumer] {push_consumer} started, waiting for messages.")
        try:
            input("Please Enter to Stop the Application.\r\n")
        except Exception as e:
            print(f"{push_consumer} raise exception: {e}")
        push_consumer.shutdown()
        print(f"{push_consumer} shutdown.")
    except Exception as e:
        print(f"{push_consumer} startup raise exception: {e}")
        if push_consumer.is_running:
            push_consumer.shutdown()
        print(f"{push_consumer} shutdown.")
