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

# 组装消息
msg = Message('rocketmq-xxx|namespace_python%topic1')
# 设置keys
msg.set_keys('yourKey')
# 设置tags
msg.set_tags('yourTags')
# 消息内容
msg.set_body('This is a oneway message1.')

# 发送单向消息
producer.send_oneway(msg)
# 模拟业务
print('Send oneway message.')
producer.shutdown()
