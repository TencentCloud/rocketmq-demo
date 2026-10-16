import time

from rocketmq.client import TransactionMQProducer, Message, TransactionStatus
from rocketmq.exceptions import RocketMQException

"""
 事务消息需要使用 事务类型的topic，请先在控制台创建事务类型的topic。
 流程：发送半消息 -> 执行本地事务(local_execute) -> 返回 COMMIT/ROLLBACK/UNKNOWN；
 返回 UNKNOWN 时，服务端会定时回查(check_callback)，直到得到 COMMIT 或 ROLLBACK。
"""

# 模拟本地事务结果存储，key 为消息的 keys
local_trans = {}


# 执行本地事务回调，签名为 (msg, user_args)，user_args 为 send_message_in_transaction 传入的 user_args
# 注意：此回调中的 msg 是底层待发送的消息（非服务端返回的 MessageExt），只读取 keys/tags/body，不要读取 msg.id 等字段
def local_execute(msg, user_args):
    key = msg.keys.decode('utf-8')
    try:
        # 模拟业务：按序号决定本地事务结果 0-UNKNOWN（等待回查） 1-COMMIT 2-ROLLBACK
        status = int(key.split('-')[-1]) % 3
        local_trans[key] = status
        print('executeLocalTransaction. key: ', key, ' body: ', msg.body.decode('utf-8'), ' status: ', status)
        return TransactionStatus.UNKNOWN
    except Exception as e:
        # 回调中抛出的异常不会被 SDK 转换为事务状态，这里显式处理并返回 UNKNOWN，等待服务端回查
        print('executeLocalTransaction failed. key: ', key, ' error: ', e)
        return TransactionStatus.UNKNOWN


# 事务回查回调，签名为 (msg)
def check_callback(msg):
    key = msg.keys.decode('utf-8')
    status = local_trans.get(key)
    print('checkLocalTransaction. messageId: ', msg.id, ' key: ', key, ' status: ', status)
    if status is None:
        # 本地没有该事务记录（例如生产者重启），返回 UNKNOWN 等待下次回查
        return TransactionStatus.UNKNOWN
    if status == 1:
        return TransactionStatus.COMMIT
    if status == 2:
        return TransactionStatus.ROLLBACK
    return TransactionStatus.UNKNOWN


# 初始化事务生产者，并设置生产组信息和事务回查回调
producer = TransactionMQProducer('TransactionProducerGroup', check_callback)
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

try:
    for i in range(3):
        # 组装消息
        msg = Message('TransactionTopic')
        # 设置keys，回查时用于查找本地事务结果
        msg.set_keys('transaction-' + str(i))
        # 设置tags
        msg.set_tags('yourTags')
        # 消息内容
        msg.set_body('This is a transaction message' + str(i) + '.')
        try:
            # 发送事务消息
            ret = producer.send_message_in_transaction(msg, local_execute, None)
            print(ret.status, ret.msg_id, ret.offset)
        except RocketMQException as e:
            print('Send transaction message failed: ', e)

    # 保持进程运行，以便接收服务端的事务回查
    while True:
        time.sleep(3600)
except KeyboardInterrupt:
    pass
finally:
    # 资源释放
    producer.shutdown()
