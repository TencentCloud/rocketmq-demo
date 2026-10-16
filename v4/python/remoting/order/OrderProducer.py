from rocketmq.client import Producer, Message

"""
 顺序消息可使用 顺序类型的topic 来实现。顺序类型有全局顺序和局部顺序两种，可根据业务类型选择顺序类型
"""

# 初始化生产者，并设置生产组信息
# orderly=True 表示顺序生产者，配合 send_orderly_with_sharding_key 按业务 key 选择队列
producer = Producer('group2', orderly=True)
# 设置服务地址
producer.set_name_server_address('rocketmq-xxx.rocketmq.ap-bj.public.tencenttdmq.com:9876')
# 设置权限（角色名和密钥）
producer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 启动生产者
producer.start()

for i in range(10):
    # 组装消息
    msg = Message('rocketmq-xxx|namespace_python%topic2')
    # 消息内容
    msg.set_body('This is a new message' + str(i) + '.')

    # 按业务 key（如订单号）发送顺序消息，相同 key 的消息进入同一队列，保证顺序
    ret = producer.send_orderly_with_sharding_key(msg, 'order-' + str(i % 3))
    print(ret.status, ret.msg_id, ret.offset)
producer.shutdown()
