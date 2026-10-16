import time

from rocketmq.client import PushConsumer, ConsumeStatus

"""
 Tag 消息：订阅表达式 'TagA || TagB' 只接收 TagA 和 TagB 的消息，TagC 的消息不会投递给该消费者组。
 订阅 '*' 表示接收全部 tag。同一消费者组内所有实例的订阅关系必须一致。
"""


# 消息处理回调
def callback(msg):
    # 模拟业务
    print('Received message. messageId: ', msg.id, ' tags: ', msg.tags, ' body: ', msg.body)
    # 消费成功回复CONSUME_SUCCESS
    return ConsumeStatus.CONSUME_SUCCESS
    # 消费失败返回RECONSUME_LATER，该消息将会被重新消费
    # return ConsumeStatus.RECONSUME_LATER


# 初始化消费者，并设置消费者组信息
consumer = PushConsumer('TagConsumerGroup')
# 设置服务地址，接入地址在控制台【集群管理】->【接入信息】中获取
consumer.set_name_server_address('rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080')
# 设置权限（角色名和密钥），在控制台【角色管理】中创建角色并获取密钥
consumer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 订阅topic，只消费 TagA 和 TagB
consumer.subscribe('TagTopic', callback, 'TagA || TagB')
print(' [Consumer] Waiting for messages.')
# 启动消费者
consumer.start()

try:
    while True:
        time.sleep(3600)
except KeyboardInterrupt:
    pass
finally:
    # 资源释放
    consumer.shutdown()
