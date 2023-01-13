import os
import sys
import time

from rocketmq.client import PushConsumer, ConsumeStatus


# 消息处理回调
from rocketmq.ffi import MessageModel


def callback(msg):
    # 模拟业务
    print('Received message. messageId: ', msg.id, ' body: ', msg.body)
    # 消费成功回复CONSUME_SUCCESS
    return ConsumeStatus.CONSUME_SUCCESS
    # 消费失败返回RECONSUME_LATER，该消息将会被重新消费
    # return ConsumeStatus.RECONSUME_LATER


# 初始化消费者，并设置消费者组信息
consumer = PushConsumer('rocketmq-xxx|namespace_python%group11')
# 设置服务地址
consumer.set_name_server_address('rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876')
# 设置权限（角色名和密钥）
consumer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 设置为集群消息模式
consumer.set_message_model(MessageModel.BROADCASTING)
# 订阅topic
consumer.subscribe('rocketmq-xxx|namespace_python%topic1', callback)
print(' [Consumer] Waiting for messages.')
# 启动消费者
consumer.start()

while True:
    time.sleep(3600)

consumer.shutdown()