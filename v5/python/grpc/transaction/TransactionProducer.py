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

from rocketmq import (ClientConfiguration, Credentials, Message, Producer,
                      TransactionChecker, TransactionResolution)


class TestChecker(TransactionChecker):

    def check(self, message: Message) -> TransactionResolution:
        print(f"do TestChecker check, {message}, commit message.")
        return TransactionResolution.COMMIT


if __name__ == '__main__':
    # 设置接入点（gRPC 地址）
    endpoints = "rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080"
    # 设置权限（AK/SK）
    credentials = Credentials("ak", "sk")
    # 如果有命名空间，通过第三个参数指定
    # config = ClientConfiguration(endpoints, credentials, "namespace")
    config = ClientConfiguration(endpoints, credentials)

    topic = "topic1"
    check_from_server = True  # 由服务端回查来确认事务状态
    producer = Producer(config, (topic,), checker=TestChecker())

    try:
        producer.startup()
    except Exception as e:
        print(f"{producer} startup raise exception: {e}")

    try:
        transaction = producer.begin_transaction()
        msg = Message()
        # 消息所属 topic
        msg.topic = topic
        msg.body = "hello, rocketmq.".encode('utf-8')
        # 消息标签（tag），topic 下的二级分类
        msg.tag = "rocketmq-send-transaction-message"
        res = producer.send(msg, transaction)
        print(f"{producer} send message success. {res}")
        if check_from_server:
            # 等待服务端通过 TransactionChecker.check 回查
            input("Please Enter to Stop the Application.\r\n")
            producer.shutdown()
            print(f"{producer} shutdown.")
        else:
            # 直接提交或回滚
            transaction.commit()
            print(f"{producer} commit message:{transaction.message_id}")
            # transaction.rollback()
            # print(f"{producer} rollback message:{transaction.message_id}")
            producer.shutdown()
            print(f"{producer} shutdown.")
    except Exception as e:
        print(f"transaction producer{producer} example raise exception: {e}")
        producer.shutdown()
        print(f"{producer} shutdown.")
