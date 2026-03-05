from rocketmq.client import Producer, Message

# 初始化生产者，并设置生产组信息
producer = Producer('group1')
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
    msg = Message('rocketmq-xxx|namespace_python%topic1')
    # 消息内容
    msg.set_body('This is a new message' + str(i) + '.')

    # 发送同步消息
    ret = producer.send_sync(msg)
    print(ret.status, ret.msg_id, ret.offset)
producer.shutdown()
