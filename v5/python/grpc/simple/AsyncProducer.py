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

from rocketmq import ClientConfiguration, Credentials, Message, Producer


def handle_send_result(result_future):
    try:
        # 不要在回调中执行耗时操作，如有需要请使用其他线程
        res = result_future.result()
        print(f"send message success, {res}")
    except Exception as exception:
        print(f"send message failed, raise exception: {exception}")


if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    topic = "topic1"
    producer = Producer(config, (topic,))

    try:
        producer.startup()
        try:
            for i in range(10):
                msg = Message()
                # 消息所属 topic
                msg.topic = topic
                msg.body = "hello, rocketmq.".encode('utf-8')
                # 消息标签（tag），topic 下的二级分类
                msg.tag = "tag"
                # 消息 key，message id 之外的另一种标记方式
                msg.keys = "keys"
                # 用户自定义属性
                msg.add_property("send", "async")
                send_result_future = producer.send_async(msg)
                send_result_future.add_done_callback(handle_send_result)
        except Exception as e:
            print(f"{producer} raise exception: {e}")
    except Exception as e:
        print(f"{producer} startup raise exception: {e}")

    input("Please Enter to Stop the Application.\r\n")
    producer.shutdown()
    print(f"{producer} shutdown.")
