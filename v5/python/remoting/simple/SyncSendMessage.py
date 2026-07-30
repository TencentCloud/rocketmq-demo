from rocketmq.client import Producer, Message

# 初始化生产者，并设置生产组信息
producer = Producer('ProducerGroup')
# 设置服务地址
producer.set_name_server_address('rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080')
# 设置权限（角色名和密钥）
producer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 启动生产者
producer.start()

# 组装消息
msg = Message('topic1')
# 设置keys
msg.set_keys('yourKey')
# 设置tags
msg.set_tags('yourTags')
# 消息内容
msg.set_body('This is a new message1.')

# 发送同步消息
ret = producer.send_sync(msg)
print(ret.status, ret.msg_id, ret.offset)
# 资源释放
producer.shutdown()
