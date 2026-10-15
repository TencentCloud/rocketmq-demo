import { Producer } from 'rocketmq-client-nodejs';
// TransactionResolution 定义在 SDK 的 protobuf 生成代码中，没有从包入口导出
import definitionPb from 'rocketmq-client-nodejs/proto/apache/rocketmq/v2/definition_pb.js';

const { TransactionResolution } = definitionPb;

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的「事务消息」类型的 Topic 名称
const transactionTopic = 'yourTransactionTopic';
// 发送完成后保持运行的时间（毫秒），用于接收服务端的事务回查；回查时机由服务端决定
const waitCheckDuration = 60000;

const producer = new Producer({
  endpoints,
  sessionCredentials,
  // 提前声明 Topic，启动时预取路由
  topic: transactionTopic,
  // 事务回查：本地事务结果未提交（如应用崩溃、返回未知）时，服务端会调用 check 确认事务状态
  checker: {
    async check(messageView) {
      console.log('check transaction, messageId=%s, body=%s', messageView.messageId, messageView.body.toString());
      // 根据业务数据（如订单状态）判断本地事务是否成功，返回 COMMIT 或 ROLLBACK
      return TransactionResolution.COMMIT;
    },
  },
});
await producer.startup();

// 模拟执行本地事务，返回 COMMIT / ROLLBACK / UNKNOWN
function executeLocalTransaction(index) {
  if (index === 0) return 'COMMIT';
  if (index === 1) return 'ROLLBACK';
  // 不提交也不回滚，等待服务端回查
  return 'UNKNOWN';
}

try {
  for (let i = 0; i < 3; i++) {
    // 每个事务只能包含一条消息
    const transaction = producer.beginTransaction();
    try {
      // 发送半消息，此时消费者不可见
      const receipt = await producer.send({
        topic: transactionTopic,
        tag: 'TagA',
        keys: [ `yourMessageKey-${i}` ],
        body: Buffer.from(`This is a transaction message, index=${i}`),
      }, transaction);
      console.log('send half message success, messageId=%s', receipt.messageId);

      const localResult = executeLocalTransaction(i);
      if (localResult === 'COMMIT') {
        // 提交后消息对消费者可见
        await transaction.commit();
        console.log('commit transaction, messageId=%s', receipt.messageId);
      } else if (localResult === 'ROLLBACK') {
        // 回滚后消息被丢弃
        await transaction.rollback();
        console.log('rollback transaction, messageId=%s', receipt.messageId);
      } else {
        console.log('local transaction unknown, wait for check, messageId=%s', receipt.messageId);
      }
    } catch (error) {
      console.error('send or end transaction failed, index=%d', i, error);
    }
  }
  console.log('wait %d ms for transaction check', waitCheckDuration);
  await new Promise(resolve => setTimeout(resolve, waitCheckDuration));
} finally {
  // 不再使用时关闭 producer
  await producer.shutdown();
}
