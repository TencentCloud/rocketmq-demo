from rocketmq.client import Producer, Message

"""
 Tag 消息：生产者为每条消息设置 tag，消费者可按 tag 过滤订阅。
 请先在控制台创建普通类型的topic。
"""

# 初始化生产者，并设置生产组信息
producer = Producer('ProducerGroup')
# 设置服务地址，接入地址在控制台【集群管理】->【接入信息】中获取
producer.set_name_server_address('rmq-xxxx.rocketmq.gz.qcloud.tencenttdmq.com:8080')
# 设置权限（角色名和密钥），在控制台【角色管理】中创建角色并获取密钥
producer.set_session_credentials(
    'eyJrZXlJZC......',
    'admin',
    ''
)
# 启动生产者
producer.start()

tags = ['TagA', 'TagB', 'TagC']
for i in range(9):
    tag = tags[i % len(tags)]
    # 组装消息
    msg = Message('TagTopic')
    # 设置keys
    msg.set_keys('yourKey' + str(i))
    # 设置tags
    msg.set_tags(tag)
    # 消息内容
    msg.set_body('This is a ' + tag + ' message' + str(i) + '.')

    # 发送同步消息
    ret = producer.send_sync(msg)
    print(tag, ret.status, ret.msg_id, ret.offset)
# 资源释放
producer.shutdown()
