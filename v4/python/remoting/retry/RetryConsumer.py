import time

from rocketmq.client import PushConsumer, ConsumeStatus

"""
 重试消息：集群消费模式（默认）下，消费回调返回 RECONSUME_LATER 时，该消息会在一段时间后重新投递，
 msg.reconsume_times 为已重试次数（首次投递为 0）。
 默认最大重试 16 次，超过后消息进入死信队列 %DLQ%消费组名（%DLQ%rocketmq-xxx|namespace_python%retry_group），可在控制台查看。
 rocketmq-client-python 2.0.0 未提供设置最大重试次数（maxReconsumeTimes）的接口，只能使用默认值。
 注意：
  1. 请显式返回 RECONSUME_LATER 触发重试，不要依赖回调抛异常：2.0.0 中回调抛出的异常会在 ctypes 回调里重新抛出，
     返回给底层 SDK 的消费状态不确定。
  2. 广播模式（MessageModel.BROADCASTING）下消费失败不会重试。
  3. 生产者的发送失败重试（retryTimesWhenSendFailed）是发送重试，与这里的消费重试无关。
"""

# 模拟业务：前 MAX_FAILED_TIMES 次消费失败，之后消费成功
MAX_FAILED_TIMES = 3


# 消息处理回调
def callback(msg):
    print('Received message. messageId: ', msg.id, ' reconsumeTimes: ', msg.reconsume_times, ' body: ', msg.body)
    if msg.reconsume_times < MAX_FAILED_TIMES:
        # 消费失败返回RECONSUME_LATER，该消息将会被重新消费
        print('Consume failed, retry later. messageId: ', msg.id)
        return ConsumeStatus.RECONSUME_LATER
    # 消费成功回复CONSUME_SUCCESS
    return ConsumeStatus.CONSUME_SUCCESS


# 初始化消费者，并设置消费者组信息（默认集群消费模式，才会重试）
consumer = PushConsumer('rocketmq-xxx|namespace_python%retry_group')
# 设置服务地址，接入地址在控制台【集群管理】->【接入信息】中获取；Topic、消费者组名称需带命名空间前缀（格式：集群ID|命名空间%名称）
consumer.set_name_server_address('rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876')
# 设置权限（角色名和密钥），在控制台【角色管理】中创建角色并获取密钥
consumer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 订阅topic
consumer.subscribe('rocketmq-xxx|namespace_python%topic1', callback, "*")
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
