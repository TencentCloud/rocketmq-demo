from rocketmq.client import Producer, Message

"""
 顺序消息可使用 顺序类型的topic 来实现。顺序类型有全局顺序和局部顺序两种，可根据业务类型选择顺序类型
"""

# 初始化生产者，并设置生产组信息
producer = Producer('group2')
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

    # 发送同步消息
    ret = producer.send_sync(msg)
    print(ret.status, ret.msg_id, ret.offset)
producer.shutdown()
