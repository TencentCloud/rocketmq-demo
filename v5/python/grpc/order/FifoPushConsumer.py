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


class FifoMessageListener(MessageListener):

    def consume(self, message: Message) -> ConsumeResult:
        # 同一个 message_group 内的消息按发送顺序依次回调，上一条处理完成前不会投递下一条
        print(f"consume fifo message, message_group={message.message_group}, "
              f"message_id={message.message_id}, body={message.body}")
        # 消费成功返回 SUCCESS
        return ConsumeResult.SUCCESS
        # 消费失败返回 FAILURE：SDK 会在本地按消费组重试策略重新消费该消息（期间阻塞同组后续消息），
        # 达到最大重试次数后转入死信队列
        # return ConsumeResult.FAILURE


if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    # 控制台创建的「顺序消息」类型的 Topic
    topic = "fifoTopic"
    # 控制台创建的消费组，需开启「顺序投递」
    consumer_group = "FifoConsumerGroup"

    # Python SDK 的 PushConsumer 没有顺序消费相关的客户端参数：
    # 启动后根据服务端下发的消费组配置（是否开启顺序投递）自动切换为顺序消费模式，
    # 消费组未开启顺序投递时按并发消费处理，消息不保证顺序
    push_consumer = PushConsumer(
        config, consumer_group, FifoMessageListener(),
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
