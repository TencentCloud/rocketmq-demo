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


class TestMessageListener(MessageListener):

    def consume(self, message: Message) -> ConsumeResult:
        print(f"consume message, {message}.")
        # 消费成功返回 SUCCESS
        return ConsumeResult.SUCCESS
        # 消费失败返回 FAILURE，该消息将会被重新消费
        # return ConsumeResult.FAILURE


if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    topic = "topic1"
    consumer_group = "ConsumerGroup"

    # 大多数情况下不需要创建太多消费者，推荐使用单例模式
    # 不再需要时关闭 PushConsumer
    push_consumer = PushConsumer(
        config, consumer_group, TestMessageListener(),
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
