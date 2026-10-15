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

from rocketmq import ClientConfiguration, Credentials, LiteSimpleConsumer

if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    # 控制台创建的「轻量消息」类型的一级 Topic 名称
    bind_topic = "parentTopic"
    # 控制台创建消费组时需选择「Lite Topic 消费」模式并绑定到正确的 Topic
    consumer_group = "ConsumerGroup"

    # 大多数情况下不需要创建太多消费者，推荐使用单例模式
    # 注意参数顺序：第一个参数是绑定的一级 Topic
    lite_simple_consumer = LiteSimpleConsumer(bind_topic, config, consumer_group)

    try:
        lite_simple_consumer.startup()
        try:
            # subscribe_lite 会发起网络请求并校验配额，可能失败：
            # 1. 网络请求失败，可以稍后重试；
            # 2. 配额校验失败（LiteSubscriptionQuotaExceededException），需要评估配额是否足够，
            #    并及时通过 unsubscribe_lite 取消不再使用的订阅以释放配额。
            lite_simple_consumer.subscribe_lite("lite-test-0")
            lite_simple_consumer.subscribe_lite("lite-test-1")
            while True:
                try:
                    # 每次长轮询的最大消息数和消息接收后的不可见时间（秒）
                    messages = lite_simple_consumer.receive(32, 15)
                    if messages is not None:
                        for msg in messages:
                            print(f"receive message, lite_topic={msg.lite_topic}, "
                                  f"message_id={msg.message_id}, body={msg.body}")
                            # 业务处理成功后 ack，否则消息会在不可见时间结束后重新投递
                            lite_simple_consumer.ack(msg)
                            print(f"{lite_simple_consumer} ack message:[{msg.message_id}].")
                except Exception as e:
                    print(f"{lite_simple_consumer} receive or ack message raise exception: {e}")
        except Exception as e:
            print(f"{lite_simple_consumer} raise exception: {e}")
            lite_simple_consumer.shutdown()
            print(f"{lite_simple_consumer} shutdown.")
    except Exception as e:
        print(f"{lite_simple_consumer} startup raise exception: {e}")
        lite_simple_consumer.shutdown()
        print(f"{lite_simple_consumer} shutdown.")
