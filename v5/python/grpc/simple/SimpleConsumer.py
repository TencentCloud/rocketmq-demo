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

from rocketmq import (ClientConfiguration, Credentials, FilterExpression,
                      SimpleConsumer)

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
    # 不再需要时关闭 SimpleConsumer
    simple_consumer = SimpleConsumer(
        config, consumer_group, {topic: FilterExpression()}
    )

    try:
        simple_consumer.startup()
        try:
            # 订阅 topic
            # simple_consumer.subscribe(topic)
            # 使用 tag 过滤
            # simple_consumer.subscribe(topic, FilterExpression("tag"))
            while True:
                try:
                    # 每次长轮询的最大消息数和消息接收后的不可见时间
                    messages = simple_consumer.receive(32, 15)
                    if messages is not None:
                        for msg in messages:
                            simple_consumer.ack(msg)
                            print(f"{simple_consumer} ack message:[{msg.message_id}].")
                except Exception as e:
                    print(f"{simple_consumer} receive or ack message raise exception: {e}")
        except Exception as e:
            print(f"{simple_consumer} raise exception: {e}")
            simple_consumer.shutdown()
            print(f"{simple_consumer} shutdown.")
    except Exception as e:
        print(f"{simple_consumer} startup raise exception: {e}")
        simple_consumer.shutdown()
        print(f"{simple_consumer} shutdown.")
