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

import time

from rocketmq import ClientConfiguration, Credentials, Message, Producer

if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    topic = "delayTopic"
    producer = Producer(config, (topic,))

    try:
        producer.startup()
        try:
            msg = Message()
            # 消息所属 topic
            msg.topic = topic
            msg.body = "hello, rocketmq.".encode('utf-8')
            # 消息标签（tag），topic 下的二级分类
            msg.tag = "rocketmq-send-delay-message"
            # 设置延迟投递时间（当前时间 + 10 秒）
            msg.delivery_timestamp = int(time.time()) + 10
            res = producer.send(msg)
            print(f"{producer} send message success. {res}")
            producer.shutdown()
            print(f"{producer} shutdown.")
        except Exception as e:
            print(f"{producer} raise exception: {e}")
            producer.shutdown()
            print(f"{producer} shutdown.")
    except Exception as e:
        print(f"{producer} startup raise exception: {e}")
        producer.shutdown()
        print(f"{producer} shutdown.")
