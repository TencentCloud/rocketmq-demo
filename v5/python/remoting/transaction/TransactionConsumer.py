import time

from rocketmq.client import PushConsumer, ConsumeStatus

"""
 事务消息的消费与普通消息一致，只有生产者提交(COMMIT)的事务消息才会被投递给消费者。
 请先在控制台创建事务类型的topic和消费者组。
"""


# 消息处理回调
def callback(msg):
    # 模拟业务
    print('Received transaction message. messageId: ', msg.id, ' keys: ', msg.keys, ' body: ', msg.body)
    # 消费成功回复CONSUME_SUCCESS
    return ConsumeStatus.CONSUME_SUCCESS
    # 消费失败返回RECONSUME_LATER，该消息将会被重新消费
    # return ConsumeStatus.RECONSUME_LATER


# 初始化消费者，并设置消费者组信息
consumer = PushConsumer('TransactionConsumerGroup')
# 设置服务地址，接入地址在控制台【集群管理】->【接入信息】中获取
consumer.set_name_server_address('rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080')
# 设置权限（角色名和密钥），在控制台【角色管理】中创建角色并获取密钥
consumer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 订阅事务类型的topic
consumer.subscribe('TransactionTopic', callback, "*")
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
