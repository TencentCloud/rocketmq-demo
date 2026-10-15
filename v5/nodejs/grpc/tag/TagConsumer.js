import { ConsumeResult, PushConsumer } from 'rocketmq-client-nodejs';

// 填写腾讯云提供的接入地址
const endpoints = 'rmq-xxx.rocketmq.gz.qcloud.tencenttdmq.com:8080';
// 添加配置的ak和sk
const sessionCredentials = {
  accessKey: 'yourAccessKey',
  accessSecret: 'yourSecretKey',
};
// 控制台创建的消费组。同一消费组内所有消费者的订阅关系（Topic + tag 表达式）必须一致
const consumerGroup = 'yourConsumerGroup';
// 控制台创建的「普通消息」类型的 Topic 名称
const topic = 'yourNormalTopic';
// tag 过滤表达式：只消费 TagA 和 TagB，TagC 的消息不会投递给该消费组；'*' 表示全部
const tagExpression = 'TagA||TagB';

const consumer = new PushConsumer({
  endpoints,
  sessionCredentials,
  consumerGroup,
  // 字符串会按 tag 表达式处理（FilterExpression 默认类型为 TAG）
  subscriptions: new Map([[ topic, tagExpression ]]),
  messageListener: {
    async consume(messageView) {
      console.log('consume message, tag=%s, messageId=%s, body=%s',
        messageView.tag, messageView.messageId, messageView.body.toString());
      return ConsumeResult.SUCCESS;
    },
  },
});
// startup() 失败会直接抛出异常并退出进程
await consumer.startup();
console.log('tag consumer started, topic=%s, tagExpression=%s', topic, tagExpression);

// Ctrl+C 退出时关闭 consumer
process.once('SIGINT', async () => {
  await consumer.shutdown();
  process.exit(0);
});
