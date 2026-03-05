import time

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
# 设置消息延迟级别
# 1s、 5s、 10s、 30s、  1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h
# 1    2    3     4     5    6   7    8   9   10   11   12  13   14    15    16   17   18
msg.set_delay_time_level(2)
# 消息内容
msg.set_body('This is a new message1.')

# 发送同步消息
ret = producer.send_sync(msg)
print(ret.status, ret.msg_id, ret.offset)
producer.shutdown()
