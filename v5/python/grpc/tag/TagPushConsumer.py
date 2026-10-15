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


class TagMessageListener(MessageListener):

    def consume(self, message: Message) -> ConsumeResult:
        print(f"consume message, tag={message.tag}, "
              f"message_id={message.message_id}, body={message.body}")
        # 消费成功返回 SUCCESS
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
    # 控制台创建的消费组。同一消费组内所有消费者的订阅关系（Topic + tag 表达式）必须一致
    consumer_group = "ConsumerGroup"
    # tag 过滤表达式：只消费 TagA 和 TagB，TagC 的消息不会投递给该消费组；"*" 表示全部
    # FilterExpression 默认过滤类型为 TAG
    tag_expression = "TagA||TagB"

    push_consumer = PushConsumer(
        config, consumer_group, TagMessageListener(),
        {topic: FilterExpression(tag_expression)}
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
